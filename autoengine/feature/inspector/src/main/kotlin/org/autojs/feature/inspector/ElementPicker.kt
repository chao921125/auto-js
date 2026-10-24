package org.autojs.feature.inspector

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import org.autojs.toolkit.floaty.FloatyManager

/**
 * 元素拾取器：悬浮窗覆盖屏幕，用户触摸点选控件，自动生成选择器代码。
 *
 * 替代旧版 cca/codegeneration/UiSelectorGenerator.java，
 * 基于新版 LayoutInspector + FloatyManager 实现。
 */
class ElementPicker(private val context: Context) {

    interface PickCallback {
        fun onPicked(node: LayoutInspector.NodeInfo, selectorCode: String)
        fun onCancelled()
    }

    private val inspector = LayoutInspector()
    private val floatyManager = FloatyManager(context)
    private var overlayView: View? = null
    private var infoView: TextView? = null
    private var callback: PickCallback? = null
    private var isActive = false

    /**
     * 启动元素拾取模式。
     */
    fun start(callback: PickCallback) {
        this.callback = callback
        isActive = true
        showOverlay()
    }

    /**
     * 停止元素拾取模式。
     */
    fun stop() {
        isActive = false
        removeOverlay()
        callback = null
    }

    private fun showOverlay() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val view = object : View(context) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP && isActive) {
                    handlePick(event.rawX.toInt(), event.rawY.toInt())
                }
                return true
            }
        }
        view.setBackgroundColor(Color.argb(30, 0, 0, 0))
        wm.addView(view, params)
        overlayView = view
    }

    private fun handlePick(x: Int, y: Int) {
        val node = inspector.pickElement(x, y)
        if (node != null) {
            val selectorCode = node.toSelectorCode()
            callback?.onPicked(node, selectorCode)
            stop()
        } else {
            callback?.onCancelled()
            stop()
        }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try { wm.removeView(view) } catch (e: Exception) { }
        overlayView = null
    }
}
