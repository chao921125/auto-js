package org.autojs.feature.scheduler

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

/**
 * 触发器管理器：监听系统事件并触发脚本执行。
 */
class TriggerManager(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private val listeners = mutableMapOf<String, () -> Unit>()

    /**
     * 监听解锁事件。
     */
    fun onUnlock(callback: () -> Unit) {
        // 注册 ACTION_USER_PRESENT 广播
    }

    /**
     * 监听充电状态变化。
     */
    fun onChargingChanged(callback: (isCharging: Boolean) -> Unit) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        // 注册广播接收器
    }

    /**
     * 监听指定 App 进入前台。
     */
    fun onAppForeground(packageName: String, callback: () -> Unit) {
        // UsageStatsManager 轮询
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isAppForeground(packageName)) callback()
                handler.postDelayed(this, 2000)
            }
        }, 2000)
    }

    /**
     * 监听通知。
     */
    fun onNotification(packageName: String, textPattern: String, callback: () -> Unit) {
        // NotificationListenerService
    }

    /**
     * 监听屏幕状态。
     */
    fun onScreenOn(callback: () -> Unit) {}
    fun onScreenOff(callback: () -> Unit) {}

    /**
     * 监听无障碍事件。
     */
    fun onAccessibilityEvent(eventType: Int, callback: (AccessibilityEvent) -> Unit) {}

    private fun isAppForeground(packageName: String): Boolean {
        // 使用 UsageStatsManager 检查
        return false
    }

    fun isScreenOn(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isInteractive
    }

    fun isCharging(): Boolean {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val battery = context.registerReceiver(null, filter)
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
               status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun isUnlocked(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Settings.Secure.getInt(context.contentResolver, "lockscreen.disabled", 0) == 1
        } else false
    }

    fun clear() {
        listeners.clear()
        handler.removeCallbacksAndMessages(null)
    }
}
