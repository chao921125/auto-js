package org.autojs.core.runtime

import org.autojs.core.api.*
import org.mozilla.javascript.*
import java.io.File
import java.security.MessageDigest

/**
 * 模块加载器：实现 require() 三级解析。
 * 1. 相对/绝对文件路径（./module, ../module, /path/to/module）
 * 2. 内置模块（auto, images, http, files, ...）
 * 3. 插件模块（通过 PluginManager 注册的外部模块）
 */
class ModuleLoader(private val scriptDir: String) {

    private val cache = mutableMapOf<String, Scriptable>()
    private val builtinModules = mutableSetOf(
        "auto", "images", "ocr", "floaty", "ui", "files", "storages",
        "http", "app", "device", "shell", "threads", "timers", "events",
        "dialogs", "console", "engines", "util", "crypto", "base64"
    )

    fun require(cx: Context, scope: Scriptable, moduleName: String): Any? {
        // 缓存命中
        val cacheKey = resolveModulePath(moduleName)
        cache[cacheKey]?.let { return it }

        // 内置模块：返回 ApiRegistry 中已注册的命名空间
        if (builtinModules.contains(moduleName)) {
            val provider = ApiRegistry.get(moduleName)
            if (provider != null) {
                val jsObj = cx.newObject(scope) as ScriptableObject
                val scriptScope = RhinoScriptScope(cx, scope as ScriptableObject, jsObj)
                provider.javaClass.getMethod("install", ScriptScope::class.java)
                    .invoke(provider, scriptScope)
                cache[cacheKey] = jsObj
                return jsObj
            }
        }

        // 文件模块
        val moduleFile = resolveFileModule(moduleName) ?: run {
            throw ScriptException("MODULE_NOT_FOUND", "Cannot find module '$moduleName'", cause = null)
        }

        val code = moduleFile.readText(Charsets.UTF_8)
        val moduleScope = cx.newObject(scope) as ScriptableObject
        moduleScope.prototype = scope as ScriptableObject

        // 注入 module/exports
        val exports = cx.newObject(scope)
        ScriptableObject.defineProperty(moduleScope, "exports", exports, ScriptableObject.EMPTY)
        ScriptableObject.defineProperty(moduleScope, "module", object : BaseFunction() {
            override fun get(name: String, start: Scriptable): Any? = when (name) {
                "exports" -> exports
                "id" -> cacheKey
                "filename" -> moduleFile.absolutePath
                else -> super.get(name, start)
            }
        }, ScriptableObject.EMPTY)

        cx.evaluateString(moduleScope, code, moduleFile.name, 1, null)
        cache[cacheKey] = exports
        return exports
    }

    private fun resolveModulePath(moduleName: String): String {
        return if (moduleName.startsWith(".")) {
            File(scriptDir, moduleName).canonicalPath
        } else moduleName
    }

    private fun resolveFileModule(moduleName: String): File? {
        val candidates = mutableListOf<File>()
        if (moduleName.startsWith(".")) {
            val base = File(scriptDir).canonicalFile
            candidates.add(File(base, moduleName))
            candidates.add(File(base, "$moduleName.js"))
            candidates.add(File(base, "$moduleName/index.js"))
        } else {
            candidates.add(File(scriptDir, "$moduleName.js"))
            candidates.add(File(scriptDir, "node_modules/$moduleName.js"))
            candidates.add(File(scriptDir, "node_modules/$moduleName/index.js"))
        }
        return candidates.firstOrNull { it.exists() && it.isFile }
    }

    fun clearCache() = cache.clear()

    fun getCacheInfo(): Map<String, String> {
        return cache.mapValues { (_, v) ->
            val md = MessageDigest.getInstance("MD5")
            md.digest(v.toString().toByteArray()).joinToString("") { "%02x".format(it) }
        }
    }
}
