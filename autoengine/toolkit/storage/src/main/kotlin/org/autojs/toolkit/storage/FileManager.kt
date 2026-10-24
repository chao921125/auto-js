package org.autojs.toolkit.storage

import java.io.*
import java.util.zip.*

/**
 * 文件管理器：脚本沙箱内的文件操作。
 * 路径统一过 ScriptFileSystem 归一化校验，防止 .. 越界。
 */
class FileManager(private val sandboxDir: String) {

    private fun resolve(path: String): File {
        val f = if (path.startsWith("/")) File(path) else File(sandboxDir, path)
        val canonical = f.canonicalFile
        // 沙箱校验：不允许访问沙箱外的文件
        val sandboxCanonical = File(sandboxDir).canonicalFile
        if (!canonical.path.startsWith(sandboxCanonical.path)) {
            throw SecurityException("Path '$path' is outside script sandbox")
        }
        return canonical
    }

    fun read(path: String): String = resolve(path).readText(Charsets.UTF_8)

    fun readBytes(path: String): ByteArray = resolve(path).readBytes()

    fun write(path: String, content: String): Boolean = try {
        resolve(path).apply { parentFile?.mkdirs() }.writeText(content, Charsets.UTF_8); true
    } catch (e: Exception) { false }

    fun writeBytes(path: String, data: ByteArray): Boolean = try {
        resolve(path).apply { parentFile?.mkdirs() }.writeBytes(data); true
    } catch (e: Exception) { false }

    fun append(path: String, content: String): Boolean = try {
        resolve(path).apply { parentFile?.mkdirs() }.appendText(content, Charsets.UTF_8); true
    } catch (e: Exception) { false }

    fun copy(src: String, dest: String): Boolean = try {
        resolve(src).copyTo(resolve(dest).apply { parentFile?.mkdirs() }, overwrite = true); true
    } catch (e: Exception) { false }

    fun move(src: String, dest: String): Boolean = try {
        resolve(src).renameTo(resolve(dest)); true
    } catch (e: Exception) { false }

    fun remove(path: String): Boolean = resolve(path).deleteRecursively()

    fun exists(path: String): Boolean = resolve(path).exists()

    fun isFile(path: String): Boolean = resolve(path).isFile
    fun isDir(path: String): Boolean = resolve(path).isDirectory

    fun list(path: String): List<String> = resolve(path).list()?.toList() ?: emptyList()

    fun size(path: String): Long = resolve(path).length()

    fun mkdir(path: String): Boolean = resolve(path).mkdirs()

    fun zip(srcPath: String, destPath: String): Boolean = try {
        val src = resolve(srcPath)
        val dest = resolve(destPath).apply { parentFile?.mkdirs() }
        ZipOutputStream(BufferedOutputStream(FileOutputStream(dest))).use { zos ->
            if (src.isDirectory) {
                src.walkTopDown().filter { it.isFile }.forEach { f ->
                    val entry = ZipEntry(f.relativeTo(src).path)
                    zos.putNextEntry(entry)
                    f.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            } else {
                zos.putNextEntry(ZipEntry(src.name))
                src.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        true
    } catch (e: Exception) { false }

    fun unzip(srcPath: String, destPath: String): Boolean = try {
        val src = resolve(srcPath)
        val dest = resolve(destPath).apply { mkdirs() }
        ZipInputStream(BufferedInputStream(FileInputStream(src))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(dest, entry.name)
                if (!entry.isDirectory) {
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { zis.copyTo(it) }
                } else outFile.mkdirs()
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        true
    } catch (e: Exception) { false }
}
