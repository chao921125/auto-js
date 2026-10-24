package org.autojs.toolkit.shell

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope

/**
 * shell 命名空间 API：Shell 命令执行。
 */
class ShellApiProvider : ApiProvider {
    override val name = "shell"
    private val executor = ShellExecutor()

    override fun install(scope: ScriptScope) {
        scope.defineObject("shell") {
            function("exec") { args ->
                val cmd = args[0]?.toString() ?: ""
                val root = args.getOrNull(1) as? Boolean ?: false
                executor.exec(cmd, root)
            }
            function("isRootAvailable") { _ -> executor.isRootAvailable() }
        }
    }
}
