package org.autojs.toolkit.shizuku

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope

/**
 * shizuku 命名空间 API：通过 Shizuku 执行高权限操作。
 *
 * 用法：
 *   shizuku.isAvailable()
 *   let r = shizuku.exec("pm list packages")
 *   shizuku.checkPermission()
 */
class ShizukuApiProvider : ApiProvider {
    override val name = "shizuku"

    private val manager = ShizukuManager()

    override fun install(scope: ScriptScope) {
        scope.defineObject("shizuku") {
            function("isAvailable") { _ -> ShizukuManager.isAvailable() }
            function("checkPermission") { _ -> ShizukuManager.checkPermission() }
            function("exec") { args ->
                val cmd = args[0]?.toString() ?: return@function null
                manager.execShell(cmd)
            }
            function("init") { _ -> manager.init(); true }
            function("destroy") { _ -> manager.destroy(); true }
        }
    }
}
