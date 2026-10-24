package org.autojs.toolkit.floaty

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.TextView
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 悬浮窗管理器：创建/管理悬浮窗，权限检查，生命周期绑定。
 */
class FloatyManager(private val context: Context) {

    private val windows = ConcurrentLinkedQueue<FloatWindow>()
    private val handler = Handler(Looper.getMainLooper())

    fun isOverlaysEnabled(): Boolean = Settings.canDrawOverlays(context)

    fun openOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * 创建悬浮窗。
     * @param view 要显示的 View
     * @param x 初始 X 坐标
     * @param y 初始 Y 坐标
     * @param width 宽度（px 或 WRAP_CONTENT）
     * @param height 高度
     */
    fun createWindow(view: View, x: Int = 0, y: Int = 0, width: Int = WRAP, height: Int = WRAP): FloatWindow {
        val params = WindowManager.LayoutParams(
            if (width == WRAP) WindowManager.LayoutParams.WRAP_CONTENT else width,
            if (height == WRAP) WindowManager.LayoutParams.WRAP_CONTENT else height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x; this.y = y
        }
        val window = FloatWindow(context, view, params)
        windows.add(window)
        handler.post { window.show() }
        return window
    }

    /**
     * 创建悬浮控制台窗口。
     */
    fun createConsole(maxLines: Int = 200): FloatConsole {
        val console = FloatConsole(context, this, maxLines)
        return console
    }

    fun closeWindow(window: FloatWindow) {
        handler.post { window.close() }
        windows.remove(window)
    }

    fun closeAll() {
        windows.forEach { handler.post { it.close() } }
        windows.clear()
    }

    companion object {
        const val WRAP = -1
        const val MATCH = -2
    }
}

/**
 * 悬浮窗实例：封装 View + LayoutParams，支持拖拽和位置更新。
 */
class FloatWindow(
    private val context: Context,
    val view: View,
    private val params: WindowManager.LayoutParams
) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var shown = false
    var onTouch: ((MotionEvent) -> Boolean)? = null

    fun show() {
        if (shown) return
        wm.addView(view, params)
        shown = true
        setupDrag()
    }

    fun close() {
        if (!shown) return
        wm.removeView(view)
        shown = false
    }

    fun setPosition(x: Int, y: Int) {
        params.x = x; params.y = y
        if (shown) wm.updateViewLayout(view, params)
    }

    fun setSize(width: Int, height: Int) {
        params.width = if (width == FloatyManager.WRAP) WindowManager.LayoutParams.WRAP_CONTENT else width
        params.height = if (height == FloatyManager.WRAP) WindowManager.LayoutParams.WRAP_CONTENT else height
        if (shown) wm.updateViewLayout(view, params)
    }

    fun isShown(): Boolean = shown

    private fun setupDrag() {
        view.setOnTouchListener { _, event ->
            onTouch?.invoke(event) ?: false
        }
    }
}
