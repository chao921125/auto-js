package org.autojs.toolkit.shell

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Shell 执行器：非 Root 用 Runtime.exec，Root 走 su 管道。
 */
class ShellExecutor {

    data class ShellResult(val code: Int, val result: String, val error: String)

    /**
     * 执行 shell 命令。
     * @param cmd 命令字符串
     * @param root 是否使用 Root 权限
     */
    fun exec(cmd: String, root: Boolean = false): ShellResult {
        return if (root) execRoot(cmd) else execNormal(cmd)
    }

    private fun execNormal(cmd: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val result = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val code = process.waitFor()
            ShellResult(code, result.trim(), error.trim())
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "exec failed")
        }
    }

    private fun execRoot(cmd: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su"))
            process.outputStream.write("$cmd\nexit\n".toByteArray())
            process.outputStream.flush()
            val result = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val code = process.waitFor()
            ShellResult(code, result.trim(), error.trim())
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "root exec failed (device may not be rooted)")
        }
    }

    /**
     * 检查设备是否已 Root。
     */
    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            process.waitFor() == 0
        } catch (e: Exception) { false }
    }
}
