package org.autojs.core.api

/**
 * 脚本执行异常：携带行列号和中文错误信息。
 */
class ScriptException(
    val code: String,
    override val message: String,
    val line: Int = -1,
    val column: Int = -1,
    val stack: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 脚本中断异常：引擎停止脚本时抛出。
 */
class ScriptInterrupt(message: String = "Script interrupted") : RuntimeException(message)

/**
 * 脚本生命周期事件。
 */
enum class ScriptEvent {
    START, EXIT, CRASH, INTERRUPT
}

/**
 * 脚本执行结果。
 */
data class ExecutionResult(
    val success: Boolean,
    val returnValue: Any? = null,
    val error: ScriptException? = null,
    val durationMs: Long = 0
)
