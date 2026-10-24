package org.autojs.toolkit.accessibility

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.api.ScriptObjectBuilder

/**
 * auto 命名空间 API 提供者：无障碍自动化脚本接口。
 * 注入到 JS: auto.text(), auto.id(), auto.click(), auto.waitFor() 等。
 */
class AutoApiProvider : ApiProvider {
    override val name = "auto"

    private val engine: AccessibilityEngine?
        get() = AutoJsAccessibilityService.instance?.getEngine()

    override fun install(scope: ScriptScope) {
        scope.defineObject("auto") {
            // 选择器方法
            function("text") { args ->
                val s = UiSelector().text(args[0]?.toString() ?: "")
                engine?.let { UiQuery(it, s) }
            }
            function("textContains") { args ->
                val s = UiSelector().textContains(args[0]?.toString() ?: "")
                engine?.let { UiQuery(it, s) }
            }
            function("id") { args ->
                val s = UiSelector().id(args[0]?.toString() ?: "")
                engine?.let { UiQuery(it, s) }
            }
            function("desc") { args ->
                val s = UiSelector().desc(args[0]?.toString() ?: "")
                engine?.let { UiQuery(it, s) }
            }
            function("clazz") { args ->
                val s = UiSelector().clazz(args[0]?.toString() ?: "")
                engine?.let { UiQuery(it, s) }
            }
            function("clickable") { args ->
                val s = UiSelector().clickable(args[0] as? Boolean ?: true)
                engine?.let { UiQuery(it, s) }
            }
            // 坐标操作
            function("click") { args ->
                val x = (args[0] as? Number)?.toFloat() ?: 0f
                val y = (args[1] as? Number)?.toFloat() ?: 0f
                engine?.gestureClick(x, y) ?: false
            }
            function("longClick") { args ->
                val x = (args[0] as? Number)?.toFloat() ?: 0f
                val y = (args[1] as? Number)?.toFloat() ?: 0f
                engine?.gestureLongClick(x, y) ?: false
            }
            function("swipe") { args ->
                val x1 = (args[0] as? Number)?.toFloat() ?: 0f
                val y1 = (args[1] as? Number)?.toFloat() ?: 0f
                val x2 = (args[2] as? Number)?.toFloat() ?: 0f
                val y2 = (args[3] as? Number)?.toFloat() ?: 0f
                val dur = (args[4] as? Number)?.toLong() ?: 300L
                engine?.gestureSwipe(x1, y1, x2, y2, dur) ?: false
            }
            function("gesture") { args ->
                val duration = (args[0] as? Number)?.toLong() ?: 200L
                @Suppress("UNCHECKED_CAST")
                val points = args[1] as? List<List<Number>> ?: return@function false
                val path = android.graphics.Path()
                points.firstOrNull()?.let { p -> path.moveTo(p[0].toFloat(), p[1].toFloat()) }
                points.drop(1).forEach { p -> path.lineTo(p[0].toFloat(), p[1].toFloat()) }
                val svc = AutoJsAccessibilityService.instance ?: return@function false
                val stroke = android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, duration)
                val gesture = android.accessibilityservice.GestureDescription.Builder().addStroke(stroke).build()
                svc.dispatchGesture(gesture, null, null)
            }
            // 等待与检查
            function("waitFor") { args ->
                val timeout = (args[0] as? Number)?.toLong() ?: 5000L
                val deadline = System.currentTimeMillis() + timeout
                while (System.currentTimeMillis() < deadline) {
                    if (AutoJsAccessibilityService.isRunning()) return@function true
                    Thread.sleep(200)
                }
                false
            }
            function("waitForService") { _ -> AutoJsAccessibilityService.isRunning() }
            function("isServiceRunning") { _ -> AutoJsAccessibilityService.isRunning() }
            // 窗口
            function("window") { args ->
                val snapshots = engine?.getAllSnapshots() ?: return@function null
                val idx = (args[0] as? Number)?.toInt() ?: 0
                snapshots.elementAtOrNull(idx)
            }
            function("windows") { _ -> engine?.getAllSnapshots()?.toList() ?: emptyList<Any>() }
            // 滚动
            function("scrollDown") { _ -> engine?.gestureSwipe(0f, 0f, 0f, 0f, 300) ?: false }
            function("scrollUp") { _ -> engine?.gestureSwipe(0f, 0f, 0f, 0f, 300) ?: false }
            // 输入
            function("input") { args ->
                val s = UiSelector().text(args[0]?.toString() ?: "")
                val query = engine?.let { UiQuery(it, s) }
                query?.setText(args[1]?.toString() ?: "") ?: false
            }
            property("version", 1)
        }
    }
}
