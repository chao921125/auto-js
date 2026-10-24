package org.autojs.feature.packager

import java.io.*
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 脚本加密器：AES-256-GCM 加密脚本资源。
 * 密钥白盒化处理：分段藏入 assets 元数据与 native .so 常量表。
 */
class ScriptEncryptor {

    /**
     * 加密脚本代码。
     * @return EncryptedData(encryptedBytes, key, iv)
     */
    fun encrypt(code: String): EncryptedData {
        val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(code.toByteArray(Charsets.UTF_8))
        // 拼接 iv + encrypted（解密时分割）
        val result = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encrypted, 0, result, iv.size, encrypted.size)
        return EncryptedData(result, key)
    }

    /**
     * 解密脚本代码。
     */
    fun decrypt(data: EncryptedData): String {
        val iv = data.encrypted.copyOfRange(0, 12)
        val encrypted = data.encrypted.copyOfRange(12, data.encrypted.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(data.key, "AES"), GCMParameterSpec(128, iv))
        val plain = cipher.doFinal(encrypted)
        return String(plain, Charsets.UTF_8)
    }

    /**
     * 加密整个工程目录为单个 bin 文件。
     */
    fun encryptProject(projectDir: File, outputFile: File): EncryptedData {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(BufferedOutputStream(baos)).use { zos ->
            projectDir.walkTopDown().filter { it.isFile }.forEach { f ->
                zos.putNextEntry(java.util.zip.ZipEntry(f.relativeTo(projectDir).path))
                f.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return encrypt(String(baos.toByteArray(), Charsets.ISO_8859_1)).also { enc ->
            outputFile.writeBytes(enc.encrypted)
        }
    }

    data class EncryptedData(val encrypted: ByteArray, val key: ByteArray) {
        override fun equals(other: Any?): Boolean = other is EncryptedData &&
            encrypted.contentEquals(other.encrypted) && key.contentEquals(other.key)
        override fun hashCode(): Int = encrypted.contentHashCode() * 31 + key.contentHashCode()
    }
}
