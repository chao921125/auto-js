package org.autojs.core.runtime

import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 事件循环：管理 setTimeout/setInterval 定时任务和 microtask 队列。
 * 语义与浏览器一致：microtask（Promise）在当前任务后清空，macrotask（timer）按时间调度。
 */
class EventLoop(private val thread: ScriptThread) {

    private val timerIdGen = AtomicInteger(0)
    private val timers = PriorityBlockingQueue<TimerTask>()
    private val microtasks = ArrayDeque<() -> Unit>()
    private val running = AtomicBoolean(true)
    private val lock = Object()

    fun setTimeout(fn: Any?, delayMs: Long): Int {
        val id = timerIdGen.incrementAndGet()
        val fireAt = System.currentTimeMillis() + delayMs
        timers.offer(TimerTask(id, fireAt, fn, interval = -1))
        synchronized(lock) { lock.notifyAll() }
        return id
    }

    fun setInterval(fn: Any?, intervalMs: Long): Int {
        val id = timerIdGen.incrementAndGet()
        val fireAt = System.currentTimeMillis() + intervalMs
        timers.offer(TimerTask(id, fireAt, fn, interval = intervalMs))
        synchronized(lock) { lock.notifyAll() }
        return id
    }

    fun clearTimer(id: Int) {
        timers.removeIf { it.id == id }
    }

    fun queueMicrotask(fn: () -> Unit) {
        synchronized(microtasks) { microtasks.addLast(fn) }
    }

    fun runLoop() {
        while (running.get() && thread.isAlive && !thread.isInterrupted) {
            // 清空 microtask 队列
            runMicrotasks()

            // 执行到期的 timer
            val now = System.currentTimeMillis()
            val dueTimers = mutableListOf<TimerTask>()
            while (true) {
                val head = timers.peek() ?: break
                if (head.fireAt <= now) {
                    val t = timers.poll() ?: break
                    dueTimers.add(t)
                } else break
            }
            for (t in dueTimers) {
                executeTimer(t)
                if (t.interval > 0 && running.get()) {
                    timers.offer(t.copy(fireAt = System.currentTimeMillis() + t.interval))
                }
            }

            // 无任务时等待
            if (timers.isEmpty() && microtasks.isEmpty()) {
                synchronized(lock) {
                    val nextFire = timers.peek()?.fireAt
                    val waitMs = if (nextFire != null) {
                        (nextFire - System.currentTimeMillis()).coerceAtLeast(1)
                    } else 100L
                    lock.wait(waitMs)
                }
            }
        }
    }

    private fun runMicrotasks() {
        while (true) {
            val fn = synchronized(microtasks) { if (microtasks.isEmpty()) null else microtasks.removeFirst() }
            if (fn == null) break
            try { fn() } catch (e: Exception) {
                thread.runtime.log("E", "Microtask error: ${e.message}")
            }
        }
    }

    private fun executeTimer(task: TimerTask) {
        try {
            val fn = task.fn ?: return
            // 如果 fn 是 Rhino Function，调用它
            if (fn is org.mozilla.javascript.Function) {
                val cx = org.mozilla.javascript.Context.getCurrentContext()
                if (cx != null) {
                    val scope = fn as org.mozilla.javascript.Scriptable
                    fn.call(cx, scope, scope, emptyArray())
                }
            }
        } catch (e: Exception) {
            thread.runtime.log("E", "Timer error: ${e.message}")
        }
    }

    fun shutdown() {
        running.set(false)
        timers.clear()
        synchronized(microtasks) { microtasks.clear() }
        synchronized(lock) { lock.notifyAll() }
    }

    fun hasPendingTasks(): Boolean = timers.isNotEmpty() || synchronized(microtasks) { microtasks.isNotEmpty() }

    private data class TimerTask(
        val id: Int,
        val fireAt: Long,
        val fn: Any?,
        val interval: Long = -1
    ) : Comparable<TimerTask> {
        override fun compareTo(other: TimerTask): Int = fireAt.compareTo(other.fireAt)
    }
}
