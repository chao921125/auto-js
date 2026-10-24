package org.autojs.core.runtime

import java.util.concurrent.ConcurrentHashMap

/**
 * 事件总线：脚本线程间通信和事件订阅。
 * 支持 events.on / events.once / events.emit / events.off。
 */
class EventBus {

    private val listeners = ConcurrentHashMap<String, MutableList<(Any?) -> Unit>>()

    fun on(event: String, handler: (Any?) -> Unit) {
        listeners.computeIfAbsent(event) { mutableListOf() }.add(handler)
    }

    fun once(event: String, handler: (Any?) -> Unit) {
        val wrapper = object : (Any?) -> Unit {
            override fun invoke(arg: Any?) {
                handler(arg)
                off(event, this)
            }
        }
        listeners.computeIfAbsent(event) { mutableListOf() }.add(wrapper)
    }

    fun emit(event: String, arg: Any? = null) {
        listeners[event]?.forEach { handler ->
            try { handler(arg) } catch (e: Exception) {
                // 静默处理监听器异常，避免影响其他监听器
            }
        }
    }

    fun off(event: String, handler: ((Any?) -> Unit)?) {
        if (handler == null) {
            listeners.remove(event)
        } else {
            listeners[event]?.remove(handler)
        }
    }

    fun clear() = listeners.clear()

    fun eventNames(): Set<String> = listeners.keys.toSet()
}
