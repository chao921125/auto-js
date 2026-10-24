package org.autojs.app.script

import android.content.Context
import java.io.File

/**
 * 脚本管理器：管理脚本文件的增删改查。
 */
class ScriptManager private constructor(private val context: Context) {

    private val scriptDir: File = File(context.filesDir, "scripts").apply { mkdirs() }

    data class ScriptInfo(
        val name: String,
        val path: String,
        val size: Long,
        val lastModified: Long
    )

    fun getAllScripts(): List<ScriptInfo> {
        return scriptDir.listFiles { f -> f.isFile && f.name.endsWith(".js") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { f ->
                ScriptInfo(f.name, f.absolutePath, f.length(), f.lastModified())
            } ?: emptyList()
    }

    fun createScript(name: String): String {
        val file = File(scriptDir, name.ensureJsExtension())
        if (!file.exists()) {
            file.writeText("// ${name}\n// ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n\"auto\";\n\n")
        }
        return file.absolutePath
    }

    fun readScript(path: String): String {
        return File(path).readText(Charsets.UTF_8)
    }

    fun writeScript(path: String, content: String) {
        File(path).writeText(content, Charsets.UTF_8)
    }

    fun deleteScript(path: String): Boolean {
        return File(path).delete()
    }

    fun renameScript(path: String, newName: String): String? {
        val oldFile = File(path)
        val newFile = File(oldFile.parentFile, newName.ensureJsExtension())
        return if (oldFile.renameTo(newFile)) newFile.absolutePath else null
    }

    fun importScript(sourcePath: String): String? {
        val srcFile = File(sourcePath)
        if (!srcFile.exists()) return null
        val destFile = File(scriptDir, srcFile.name)
        srcFile.copyTo(destFile, overwrite = true)
        return destFile.absolutePath
    }

    fun exportScript(path: String, destDir: String): Boolean {
        return try {
            File(path).copyTo(File(destDir, File(path).name), overwrite = true)
            true
        } catch (e: Exception) { false }
    }

    fun getScriptDir(): File = scriptDir

    private fun String.ensureJsExtension(): String =
        if (endsWith(".js")) this else "$this.js"

    companion object {
        @Volatile
        private var instance: ScriptManager? = null

        fun get(context: Context): ScriptManager {
            return instance ?: synchronized(this) {
                instance ?: ScriptManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
