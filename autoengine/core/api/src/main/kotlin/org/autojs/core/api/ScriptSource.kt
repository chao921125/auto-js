package org.autojs.core.api

/**
 * 脚本来源：文件路径 / 内存代码 / 加密资源。
 */
sealed class ScriptSource {
    abstract val name: String
    abstract fun getCode(): String

    data class FileSource(
        override val name: String,
        val path: String
    ) : ScriptSource() {
        override fun getCode(): String = java.io.File(path).readText(Charsets.UTF_8)
    }

    data class MemorySource(
        override val name: String,
        val scriptCode: String
    ) : ScriptSource() {
        override fun getCode(): String = scriptCode
    }

    data class EncryptedSource(
        override val name: String,
        val encryptedData: ByteArray,
        val decryptKey: ByteArray
    ) : ScriptSource() {
        override fun getCode(): String = decrypt()

        private fun decrypt(): String {
            // AES-256-GCM 解密
            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = javax.crypto.spec.SecretKeySpec(decryptKey, "AES")
            val iv = encryptedData.copyOfRange(0, 12)
            val tag = encryptedData.copyOfRange(12, encryptedData.size)
            val gcmSpec = javax.crypto.spec.GCMParameterSpec(128, iv)
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, gcmSpec)
            val plain = cipher.doFinal(tag)
            return String(plain, Charsets.UTF_8)
        }
    }
}

/**
 * 运行时配置：控制脚本执行环境的各项参数。
 */
data class RuntimeConfig(
    val optimizationLevel: Int = -1,
    val languageVersion: Int = -1, // Rhino: Context.VERSION_ES6
    val instructionLimit: Int = 0, // 0 = 不限制
    val enableSandbox: Boolean = true,
    val scriptDir: String = "",
    val maxThreads: Int = 16,
    val maxTimers: Int = 1000,
    val executionTimeoutMs: Long = 0L // 0 = 不限制
)
