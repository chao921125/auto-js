package org.autojs.feature.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import java.util.concurrent.ConcurrentHashMap

/**
 * 定时任务调度器：管理脚本的定时执行和触发器。
 */
class TaskScheduler(private val context: Context) {

    private val tasks = ConcurrentHashMap<String, ScheduledTask>()

    data class ScheduledTask(
        val id: String,
        val name: String,
        val scriptPath: String,
        val trigger: Trigger,
        val enabled: Boolean = true,
        val retryCount: Int = 3,
        val retryDelayMs: Long = 60000
    )

    sealed class Trigger {
        data class Interval(val intervalMs: Long) : Trigger()
        data class Daily(val hour: Int, val minute: Int) : Trigger()
        data class Weekly(val dayOfWeek: Int, val hour: Int, val minute: Int) : Trigger()
        data class Cron(val expression: String) : Trigger()
        data class OnUnlock(val scriptPath: String) : Trigger()
        data class OnCharging(val scriptPath: String) : Trigger()
        data class OnAppForeground(val packageName: String, val scriptPath: String) : Trigger()
        data class OnNotification(val packageName: String, val textPattern: String, val scriptPath: String) : Trigger()
    }

    fun addTask(task: ScheduledTask) {
        tasks[task.id] = task
        scheduleTask(task)
    }

    fun removeTask(id: String) {
        tasks.remove(id)
        // 取消对应的 AlarmManager / WorkManager
    }

    fun getTask(id: String): ScheduledTask? = tasks[id]
    fun getAllTasks(): List<ScheduledTask> = tasks.values.toList()

    fun enableTask(id: String, enabled: Boolean) {
        tasks[id]?.let { task ->
            tasks[id] = task.copy(enabled = enabled)
            if (enabled) scheduleTask(task) else cancelTask(task)
        }
    }

    private fun scheduleTask(task: ScheduledTask) {
        if (!task.enabled) return
        // 根据 trigger 类型设置不同的调度方式
        when (val trigger = task.trigger) {
            is Trigger.Interval -> scheduleInterval(task, trigger.intervalMs)
            is Trigger.Daily -> scheduleDaily(task, trigger.hour, trigger.minute)
            is Trigger.Weekly -> scheduleWeekly(task, trigger.dayOfWeek, trigger.hour, trigger.minute)
            is Trigger.Cron -> scheduleCron(task, trigger.expression)
            is Trigger.OnUnlock -> registerUnlockReceiver(task)
            is Trigger.OnCharging -> registerChargingReceiver(task)
            is Trigger.OnAppForeground -> registerAppForegroundWatcher(task, trigger.packageName)
            is Trigger.OnNotification -> registerNotificationListener(task, trigger.packageName, trigger.textPattern)
        }
    }

    private fun cancelTask(task: ScheduledTask) {
        // 取消调度
    }

    private fun scheduleInterval(task: ScheduledTask, intervalMs: Long) {
        // 使用 WorkManager 周期任务（>=15min）或 AlarmManager
    }

    private fun scheduleDaily(task: ScheduledTask, hour: Int, minute: Int) {
        // AlarmManager.setExactAndAllowWhileIdle
    }

    private fun scheduleWeekly(task: ScheduledTask, dayOfWeek: Int, hour: Int, minute: Int) {
        // AlarmManager 每周触发
    }

    private fun scheduleCron(task: ScheduledTask, expression: String) {
        // 解析 Cron 表达式并设置 AlarmManager
    }

    private fun registerUnlockReceiver(task: ScheduledTask) {
        // ACTION_USER_PRESENT 广播接收器
    }

    private fun registerChargingReceiver(task: ScheduledTask) {
        // BatteryManager 充电状态监听
    }

    private fun registerAppForegroundWatcher(task: ScheduledTask, packageName: String) {
        // UsageStatsManager 轮询或无障碍窗口事件
    }

    private fun registerNotificationListener(task: ScheduledTask, pkg: String, pattern: String) {
        // NotificationListenerService
    }
}

/**
 * 开机自启动接收器。
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 恢复定时任务调度
            val scheduler = TaskScheduler(context)
            // 从数据库加载已保存的任务并重新调度
        }
    }
}
