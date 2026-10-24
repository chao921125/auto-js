package org.autojs.toolkit.storage

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope

/**
 * files 命名空间 API：文件读写/复制/压缩/遍历。
 */
class FilesApiProvider(private val sandboxDir: String) : ApiProvider {
    override val name = "files"
    private val fm = FileManager(sandboxDir)

    override fun install(scope: ScriptScope) {
        scope.defineObject("files") {
            function("read") { args -> fm.read(args[0]?.toString() ?: "") }
            function("write") { args -> fm.write(args[0]?.toString() ?: "", args[1]?.toString() ?: "") }
            function("append") { args -> fm.append(args[0]?.toString() ?: "", args[1]?.toString() ?: "") }
            function("copy") { args -> fm.copy(args[0]?.toString() ?: "", args[1]?.toString() ?: "") }
            function("move") { args -> fm.move(args[0]?.toString() ?: "", args[1]?.toString() ?: "") }
            function("remove") { args -> fm.remove(args[0]?.toString() ?: "") }
            function("exists") { args -> fm.exists(args[0]?.toString() ?: "") }
            function("isFile") { args -> fm.isFile(args[0]?.toString() ?: "") }
            function("isDir") { args -> fm.isDir(args[0]?.toString() ?: "") }
            function("list") { args -> fm.list(args[0]?.toString() ?: "") }
            function("size") { args -> fm.size(args[0]?.toString() ?: "") }
            function("mkdir") { args -> fm.mkdir(args[0]?.toString() ?: "") }
            function("zip") { args -> fm.zip(args[0]?.toString() ?: "", args[1]?.toString() ?: "") }
            function("unzip") { args -> fm.unzip(args[0]?.toString() ?: "", args[1]?.toString() ?: "") }
            function("readBytes") { args -> fm.readBytes(args[0]?.toString() ?: "") }
            function("writeBytes") { args ->
                @Suppress("UNCHECKED_CAST")
                val data = args[1] as? ByteArray ?: return@function false
                fm.writeBytes(args[0]?.toString() ?: "", data)
            }
        }
    }
}

/**
 * storages 命名空间 API：键值持久化。
 */
class StoragesApiProvider : ApiProvider {
    override val name = "storages"
    private val manager = StorageManager()

    override fun install(scope: ScriptScope) {
        scope.defineObject("storages") {
            function("create") { args -> manager.create(args[0]?.toString() ?: "default") }
            function("remove") { args -> manager.remove(args[0]?.toString() ?: ""); null }
        }
    }
}
