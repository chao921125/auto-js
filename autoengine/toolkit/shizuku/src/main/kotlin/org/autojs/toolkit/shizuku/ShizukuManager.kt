package org.autojs.toolkit.shizuku

/**
 * Shizuku 管理器：通过反射调用 Shizuku API，避免编译时依赖问题。
 *
 * 替代旧版 cca/shizuku/WrappedShizuku.java，
 * 使用 Shizuku SDK 13.x（RikkaApps 维护，活跃更新）。
 * 运行时需要 Shizuku SDK 在 classpath 中。
 */
class ShizukuManager {

    companion object {
        @Volatile
        var isBinderReceived = false
            private set

        private val shizukuClass: Class<*>? by lazy {
            try { Class.forName("dev.rikka.shizuku.Shizuku") } catch (e: Exception) { null }
        }

        fun isAvailable(): Boolean = try {
            shizukuClass?.getMethod("pingBinder")?.invoke(null) as? Boolean ?: false
        } catch (e: Exception) { false }

        fun checkPermission(): Boolean = try {
            val cls = shizukuClass ?: return false
            val isPreV11 = cls.getMethod("isPreV11").invoke(null) as Boolean
            val perm = if (isPreV11) 1 else 2
            cls.getMethod("verifyPermission", Int::class.javaPrimitiveType).invoke(null, perm)
            true
        } catch (e: Exception) { false }
    }

    private var binderReceivedListener: Any? = null
    private var binderDeadListener: Any? = null

    fun init() {
        val cls = shizukuClass ?: return
        try {
            val receivedIf = Class.forName("dev.rikka.shizuku.Shizuku\$OnBinderReceivedListener")
            val deadIf = Class.forName("dev.rikka.shizuku.Shizuku\$OnBinderDeadListener")
            binderReceivedListener = java.lang.reflect.Proxy.newProxyInstance(
                cls.classLoader, arrayOf(receivedIf)
            ) { _, _, _ -> isBinderReceived = true; null }
            binderDeadListener = java.lang.reflect.Proxy.newProxyInstance(
                cls.classLoader, arrayOf(deadIf)
            ) { _, _, _ -> isBinderReceived = false; null }
            cls.getMethod("addBinderReceivedListenerSticky", receivedIf).invoke(null, binderReceivedListener)
            cls.getMethod("addBinderDeadListener", deadIf).invoke(null, binderDeadListener)
        } catch (e: Exception) { }
    }

    fun destroy() {
        val cls = shizukuClass ?: return
        try {
            val receivedIf = Class.forName("dev.rikka.shizuku.Shizuku\$OnBinderReceivedListener")
            val deadIf = Class.forName("dev.rikka.shizuku.Shizuku\$OnBinderDeadListener")
            if (binderReceivedListener != null)
                cls.getMethod("removeBinderReceivedListenerSticky", receivedIf).invoke(null, binderReceivedListener)
            if (binderDeadListener != null)
                cls.getMethod("removeBinderDeadListener", deadIf).invoke(null, binderDeadListener)
        } catch (e: Exception) { }
    }

    fun execShell(command: String): ShellResult {
        if (!checkPermission()) return ShellResult(-1, "", "Shizuku permission denied")
        val pb = ProcessBuilder("sh", "-c", command)
        pb.redirectErrorStream(false)
        val process = pb.start()
        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        val code = process.waitFor()
        return ShellResult(code, output, error)
    }

    data class ShellResult(val code: Int, val output: String, val error: String)
}
