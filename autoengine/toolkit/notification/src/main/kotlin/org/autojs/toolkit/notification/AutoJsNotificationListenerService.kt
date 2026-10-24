package org.autojs.toolkit.notification

import android.app.Notification as AndroidNotification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 通知监听服务：监听系统通知并触发回调。
 *
 * 替代旧版 automator/notification/NotificationListenerService.kt，
 * 使用现代 NotificationListenerService API。
 */
class AutoJsNotificationListenerService : NotificationListenerService() {

    companion object {
        @Volatile
        var instance: AutoJsNotificationListenerService? = null
            private set

        fun isRunning(): Boolean = instance != null
    }

    interface NotificationCallback {
        fun onNotificationPosted(notification: NotificationInfo)
        fun onNotificationRemoved(notification: NotificationInfo)
    }

    data class NotificationInfo(
        val packageName: String,
        val title: String,
        val text: String,
        val timestamp: Long,
        val key: String
    )

    private val callbacks = CopyOnWriteArrayList<NotificationCallback>()
    private val notificationCache = ConcurrentHashMap<String, NotificationInfo>()

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
    }

    override fun onListenerDisconnected() {
        instance = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val info = extractInfo(sbn) ?: return
        notificationCache[info.key] = info
        callbacks.forEach { it.onNotificationPosted(info) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        val info = notificationCache.remove(sbn.key)
        if (info != null) callbacks.forEach { it.onNotificationRemoved(info) }
    }

    fun addCallback(callback: NotificationCallback) { callbacks.add(callback) }
    fun removeCallback(callback: NotificationCallback) { callbacks.remove(callback) }

    fun getAllNotifications(): List<NotificationInfo> =
        notificationCache.values.toList()

    fun dismissNotification(key: String) {
        cancelNotification(key)
    }

    private fun extractInfo(sbn: StatusBarNotification): NotificationInfo? {
        val n = sbn.notification ?: return null
        val extras = n.extras ?: return null
        return NotificationInfo(
            packageName = sbn.packageName,
            title = extras.getCharSequence(AndroidNotification.EXTRA_TITLE)?.toString() ?: "",
            text = extras.getCharSequence(AndroidNotification.EXTRA_TEXT)?.toString() ?: "",
            timestamp = sbn.postTime,
            key = sbn.key
        )
    }
}
