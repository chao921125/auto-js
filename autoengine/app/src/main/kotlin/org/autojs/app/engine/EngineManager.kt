package org.autojs.app.engine

import android.content.Context
import org.autojs.core.api.ApiRegistry
import org.autojs.core.api.RuntimeConfig
import org.autojs.core.api.ScriptSource
import org.autojs.core.runtime.ScriptRuntime
import org.autojs.toolkit.accessibility.AutoApiProvider
import org.autojs.toolkit.device.AppApiProvider
import org.autojs.toolkit.device.DeviceApiProvider
import org.autojs.toolkit.floaty.FloatyApiProvider
import org.autojs.toolkit.storage.FilesApiProvider
import org.autojs.toolkit.storage.StoragesApiProvider
import org.autojs.toolkit.network.HttpApiProvider
import org.autojs.toolkit.shell.ShellApiProvider
import org.autojs.toolkit.vision.ImagesApiProvider
import org.autojs.toolkit.vision.OcrApiProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * 引擎管理器：全局单例，负责注册所有 API、管理脚本运行时。
 */
class EngineManager(private val context: Context) {

    private val runtimes = ConcurrentHashMap<String, ScriptRuntime>()

    init {
        registerApis()
    }

    private fun registerApis() {
        val scriptDir = context.filesDir.absolutePath + "/scripts"
        ApiRegistry.apply {
            register(AutoApiProvider())
            register(ImagesApiProvider())
            register(OcrApiProvider())
            register(HttpApiProvider())
            register(FilesApiProvider(scriptDir))
            register(StoragesApiProvider())
            register(DeviceApiProvider(context))
            register(AppApiProvider(context))
            register(ShellApiProvider())
            register(FloatyApiProvider(context))
        }
    }

    /**
     * 执行脚本文件。
     */
    fun executeScript(path: String, config: RuntimeConfig = RuntimeConfig()): String {
        val source = ScriptSource.FileSource(
            name = java.io.File(path).name,
            path = path
        )
        return executeSource(source, config)
    }

    /**
     * 执行脚本代码。
     */
    fun executeCode(code: String, name: String = "inline", config: RuntimeConfig = RuntimeConfig()): String {
        val source = ScriptSource.MemorySource(name, code)
        return executeSource(source, config)
    }

    private fun executeSource(source: ScriptSource, config: RuntimeConfig): String {
        val runtime = ScriptRuntime(source, config.copy(scriptDir = context.filesDir.absolutePath + "/scripts"))
        runtimes[runtime.id] = runtime
        Thread {
            runtime.run()
            runtimes.remove(runtime.id)
        }.start()
        return runtime.id
    }

    fun stopScript(id: String): Boolean {
        runtimes[id]?.stop()
        return runtimes.remove(id) != null
    }

    fun stopAll() {
        runtimes.values.forEach { it.stop() }
        runtimes.clear()
    }

    fun getRuntime(id: String): ScriptRuntime? = runtimes[id]

    fun getAllRuntimes(): Collection<ScriptRuntime> = runtimes.values

    fun isRunning(): Boolean = runtimes.isNotEmpty()

    companion object {
        @Volatile
        private var instance: EngineManager? = null

        fun get(context: Context): EngineManager {
            return instance ?: synchronized(this) {
                instance ?: EngineManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
