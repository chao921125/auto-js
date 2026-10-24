package org.autojs.core.runtime

import org.autojs.core.api.RuntimeConfig
import org.mozilla.javascript.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rhino 引擎封装：统一 Context 创建与配置。
 * Android 上必须 optimizationLevel=-1（解释执行），因为 ART 无运行时字节码注入能力。
 */
object RhinoEngine {

    private val contextCount = AtomicInteger(0)
    @Volatile
    private var factoryInitialized = false

    init {
        if (!factoryInitialized) {
            ContextFactory.initGlobal(SandboxContextFactory())
            factoryInitialized = true
        }
    }

    fun enterContext(config: RuntimeConfig = RuntimeConfig()): Context {
        val cx = Context.enter()
        cx.optimizationLevel = config.optimizationLevel // -1 for Android
        cx.languageVersion = if (config.languageVersion > 0) config.languageVersion else Context.VERSION_ES6

        if (config.enableSandbox) {
            try {
                val setter = Context::class.java.getMethod("setClassShutter", ClassShutter::class.java)
                setter.invoke(cx, SandboxShutter())
            } catch (e: Exception) { /* sandbox optional */ }
        }

        cx.wrapFactory = AutojsWrapFactory()

        if (config.instructionLimit > 0) {
            cx.setGenerateObserverCount(true)
            cx.instructionObserverThreshold = config.instructionLimit
        }

        contextCount.incrementAndGet()
        return cx
    }

    fun exitContext() {
        Context.exit()
        contextCount.decrementAndGet()
    }

    fun activeContextCount(): Int = contextCount.get()
}

/**
 * 沙箱 ClassShutter：禁止脚本触达危险 Java 类。
 * 仅放行 org.autojs.* 包和少量安全工具类。
 */
class SandboxShutter : ClassShutter {
    private val allowedPrefixes = setOf(
        "org.autojs.",
        "java.lang.String", "java.lang.Integer", "java.lang.Long",
        "java.lang.Double", "java.lang.Float", "java.lang.Boolean",
        "java.lang.Number", "java.lang.Math", "java.lang.Object",
        "java.util.ArrayList", "java.util.HashMap", "java.util.List",
        "java.util.Map", "java.util.Date", "java.util.regex.Pattern",
        "java.io.File", "java.io.ByteArrayInputStream", "java.io.ByteArrayOutputStream",
        "kotlin."
    )

    private val blockedPrefixes = setOf(
        "java.lang.Runtime", "java.lang.ProcessBuilder", "java.lang.System",
        "java.lang.ClassLoader", "java.lang.Thread",
        "java.lang.reflect.", "java.lang.invoke.",
        "android.app.Activity", "android.content.Context"
    )

    override fun visibleToScripts(className: String): Boolean {
        if (blockedPrefixes.any { className.startsWith(it) }) return false
        return allowedPrefixes.any { className.startsWith(it) }
    }
}

/**
 * 自定义 WrapFactory：Java 对象 → JS 友好包装。
 */
class AutojsWrapFactory : WrapFactory() {
    init {
        isJavaPrimitiveWrap = false
    }
}

/**
 * 沙箱 ContextFactory：支持指令计数看门狗。
 */
class SandboxContextFactory : ContextFactory() {
    override fun observeInstructionCount(cx: Context, instructionCount: Int) {
        val rt = Thread.currentThread() as? ScriptThread
        if (rt != null && !rt.runtime.isAlive()) {
            throw InterruptedException("Script interrupted")
        }
    }
}
