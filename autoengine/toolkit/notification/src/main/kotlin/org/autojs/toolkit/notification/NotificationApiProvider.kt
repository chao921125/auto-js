package org.autojs.toolkit.notification

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.api.ScriptObjectBuilder

/**
 * notifications 命名空间 API：通知监听与操作。
 *
 * 用法：
 *   notifications.on("posted", function(n) { log(n.title + ": " + n.text) })
 *   notifications.getActive()
 *   notifications.cancel(key)
 */
class NotificationApiProvider : ApiProvider {
    override val name = "notifications"

    private val listeners = mutableMapOf<String, (AutoJsNotificationListenerService.NotificationInfo) -> Unit>()

    override fun install(scope: ScriptScope) {
        scope.defineObject("notifications") {
            function("on") { args ->
                val event = args[0]?.toString() ?: return@function false
                @Suppress("UNCHECKED_CAST")
                val handler = args[1] as? ((AutoJsNotificationListenerService.NotificationInfo) -> Unit)
                    ?: return@function false
                listeners[event] = handler
                val service = AutoJsNotificationListenerService.instance ?: return@function false
                val callback = object : AutoJsNotificationListenerService.NotificationCallback {
                    override fun onNotificationPosted(info: AutoJsNotificationListenerService.NotificationInfo) {
                        if (event == "posted") handler(info)
                    }
                    override fun onNotificationRemoved(info: AutoJsNotificationListenerService.NotificationInfo) {
                        if (event == "removed") handler(info)
                    }
                }
                service.addCallback(callback)
                true
            }
            function("getActive") { _ ->
                val service = AutoJsNotificationListenerService.instance ?: return@function emptyList<Any>()
                service.getAllNotifications().map { n ->
                    mapOf(
                        "packageName" to n.packageName,
                        "title" to n.title,
                        "text" to n.text,
                        "timestamp" to n.timestamp,
                        "key" to n.key
                    )
                }
            }
            function("cancel") { args ->
                val key = args[0]?.toString() ?: return@function false
                val service = AutoJsNotificationListenerService.instance ?: return@function false
                service.cancelNotification(key); true
            }
            function("isRunning") { _ ->
                AutoJsNotificationListenerService.isRunning()
            }
        }
    }
}
