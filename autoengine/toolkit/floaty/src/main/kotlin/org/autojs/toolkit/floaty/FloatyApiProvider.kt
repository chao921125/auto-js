package org.autojs.toolkit.floaty

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope

/**
 * floaty 命名空间 API：悬浮窗。
 */
class FloatyApiProvider(private val context: android.content.Context) : ApiProvider {
    override val name = "floaty"
    private val manager = FloatyManager(context)

    override fun install(scope: ScriptScope) {
        scope.defineObject("floaty") {
            function("window") { args ->
                val xml = args[0]?.toString() ?: return@function null
                val renderer = XmlViewRenderer(context)
                val view = renderer.render(xml)
                manager.createWindow(view)
            }
            function("close") { args ->
                args[0]?.let { manager.closeWindow(it as FloatWindow) }; null
            }
            function("closeAll") { _ -> manager.closeAll(); null }
            function("isOverlaysEnabled") { _ -> manager.isOverlaysEnabled() }
            function("openOverlaySettings") { _ -> manager.openOverlaySettings() }
            function("console") { args ->
                val console = manager.createConsole()
                console.show()
                console
            }
            property("manager", manager)
        }
    }
}
