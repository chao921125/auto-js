package org.autojs.toolkit.floaty

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 悬浮控制台：显示脚本日志，支持拖拽和自动隐藏。
 */
class FloatConsole(
    private val context: Context,
    private val manager: FloatyManager,
    private val maxLines: Int = 200
) {
    private val logBuffer = ConcurrentLinkedQueue<LogEntry>()
    private val handler = Handler(Looper.getMainLooper())
    private var window: FloatWindow? = null
    private var logView: TextView? = null

    data class LogEntry(val level: String, val text: String, val time: Long = System.currentTimeMillis())

    fun show() {
        if (window != null) return
        val view = TextView(context).apply {
            setPadding(16, 8, 16, 8)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(200, 0, 0, 0))
            textSize = 12f
            gravity = Gravity.START
            maxLines = maxLines
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_MOVE) {
                    window?.setPosition(event.rawX.toInt(), event.rawY.toInt())
                }
                false
            }
        }
        logView = view
        window = manager.createWindow(view, 0, 100, 600, 400)
    }

    fun hide() {
        window?.let { manager.closeWindow(it) }
        window = null; logView = null
    }

    fun log(level: String, msg: String) {
        val entry = LogEntry(level, msg)
        logBuffer.add(entry)
        while (logBuffer.size > maxLines) logBuffer.poll()
        handler.post {
            val text = logBuffer.joinToString("\n") { e ->
                val color = when (e.level) {
                    "E" -> "🔴"; "W" -> "🟡"; else -> "⚪"
                }
                "$color ${e.text}"
            }
            logView?.text = text
        }
    }

    fun clear() {
        logBuffer.clear()
        handler.post { logView?.text = "" }
    }
}
