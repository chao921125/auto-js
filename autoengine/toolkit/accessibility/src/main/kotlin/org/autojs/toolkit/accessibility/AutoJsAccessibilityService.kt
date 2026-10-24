package org.autojs.toolkit.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.ConcurrentHashMap

/**
 * 无障碍服务：核心自动化引擎入口。
 * 负责接收系统无障碍事件、维护窗口快照、执行控件操作与手势注入。
 */
class AutoJsAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: AutoJsAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null
    }

    private val engine = AccessibilityEngine()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        engine.onConnected(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        engine.onEvent(event)
    }

    override fun onInterrupt() {
        engine.onInterrupt()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        engine.onDisconnected()
        return super.onUnbind(intent)
    }

    fun getEngine(): AccessibilityEngine = engine
}
