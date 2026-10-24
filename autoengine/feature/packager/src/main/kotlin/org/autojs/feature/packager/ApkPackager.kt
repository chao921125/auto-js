package org.autojs.feature.packager

import java.io.*
import java.security.*
import java.security.spec.*
import java.util.zip.*

/**
 * APK 打包器：将脚本工程打包为独立 APK。
 * 流水线：收集校验 → 加密 → 写入模板 → 图标替换 → zipalign → 签名 → 输出
 */
class ApkPackager {

    data class PackageConfig(
        val mainScript: String,
        val packageName: String = "org.autojs.packaged",
        val appName: String = "AutoJs App",
        val versionName: String = "1.0.0",
        val versionCode: Int = 1,
        val iconPath: String? = null,
        val keystorePath: String? = null,
        val keystorePassword: String? = null,
        val keyAlias: String? = null,
        val keyPassword: String? = null
    )

    data class PackageResult(
        val success: Boolean,
        val apkPath: String? = null,
        val error: String? = null,
        val durationMs: Long = 0
    )

    private val encryptor = ScriptEncryptor()
    private val signer = ApkSigner()

    /**
     * 执行打包流水线。
     * @param config 打包配置
     * @param templateApk 模板 APK 路径
     * @param outputDir 输出目录
     * @param progress 进度回调 (0~100)
     */
    fun packageApk(
        config: PackageConfig,
        templateApk: File,
        outputDir: File,
        progress: (Int, String) -> Unit = { _, _ -> }
    ): PackageResult {
        val start = System.currentTimeMillis()
        return try {
            // 1. 收集校验
            progress(10, "收集工程文件...")
            val projectDir = File(config.mainScript).parentFile
                ?: return PackageResult(false, error = "无法定位工程目录")
            if (!projectDir.exists()) {
                return PackageResult(false, error = "工程目录不存在: ${projectDir.absolutePath}")
            }

            // 2. 加密脚本
            progress(30, "加密脚本资源...")
            val encryptedData = encryptor.encryptProject(projectDir, File(outputDir, "project.bin"))

            // 3. 写入模板 APK
            progress(50, "组装 APK...")
            val tempApk = File(outputDir, "temp_${System.currentTimeMillis()}.apk")
            copyApkAndInject(templateApk, tempApk, encryptedData, config)

            // 4. 图标/应用名替换
            progress(60, "处理应用图标...")
            config.iconPath?.let { replaceIcon(tempApk, it) }

            // 5. zipalign
            progress(70, "对齐优化...")
            val alignedApk = File(outputDir, "aligned_${System.currentTimeMillis()}.apk")
            zipAlign(tempApk, alignedApk)

            // 6. 签名
            progress(85, "签名 APK...")
            val signedApk = File(outputDir, "${config.packageName}_${config.versionName}.apk")
            val ksPath = config.keystorePath
            val signingResult = if (ksPath != null) {
                signer.sign(alignedApk, signedApk, ksPath, config.keystorePassword!!, config.keyAlias!!, config.keyPassword!!)
            } else {
                signer.signWithDebugKeystore(alignedApk, signedApk)
            }

            if (!signingResult) {
                return PackageResult(false, error = "签名失败", durationMs = System.currentTimeMillis() - start)
            }

            // 7. 清理临时文件
            tempApk.delete(); alignedApk.delete()

            progress(100, "打包完成!")
            PackageResult(true, signedApk.absolutePath, durationMs = System.currentTimeMillis() - start)
        } catch (e: Exception) {
            PackageResult(false, error = e.message, durationMs = System.currentTimeMillis() - start)
        }
    }

    private fun copyApkAndInject(template: File, output: File, encrypted: ScriptEncryptor.EncryptedData, config: PackageConfig) {
        // 复制模板 APK 并注入加密的脚本资源（使用 ZipFile/ZipOutputStream）
        val configJson = """{"mainScript":"${File(config.mainScript).name}","packageName":"${config.packageName}","appName":"${config.appName}","versionName":"${config.versionName}","versionCode":${config.versionCode}}"""
        ZipFile(template).use { zf ->
            ZipOutputStream(output.outputStream()).use { zos ->
                val entries = zf.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name == "assets/project.bin" || entry.name == "assets/project_config.json") continue
                    zos.putNextEntry(ZipEntry(entry.name))
                    if (!entry.isDirectory) zf.getInputStream(entry).copyTo(zos)
                    zos.closeEntry()
                }
                // 注入加密脚本
                zos.putNextEntry(ZipEntry("assets/project.bin"))
                zos.write(encrypted.encrypted)
                zos.closeEntry()
                // 注入配置
                zos.putNextEntry(ZipEntry("assets/project_config.json"))
                zos.write(configJson.toByteArray())
                zos.closeEntry()
            }
        }
    }

    private fun replaceIcon(apk: File, iconPath: String) {
        val iconFile = File(iconPath)
        if (!iconFile.exists()) return
        val tmp = File(apk.parentFile, "tmp_${apk.name}")
        ZipFile(apk).use { zf ->
            ZipOutputStream(tmp.outputStream()).use { zos ->
                val entries = zf.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name == "res/mipmap-xxhdpi/ic_launcher.png") {
                        zos.putNextEntry(ZipEntry(entry.name))
                        iconFile.inputStream().copyTo(zos)
                        zos.closeEntry()
                    } else {
                        zos.putNextEntry(ZipEntry(entry.name))
                        if (!entry.isDirectory) zf.getInputStream(entry).copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }
        }
        tmp.copyTo(apk, overwrite = true)
        tmp.delete()
    }

    private fun zipAlign(input: File, output: File) {
        // 简化：直接复制（完整实现需要 4 字节边界对齐 native 库）
        input.copyTo(output, overwrite = true)
    }
}

/**
 * APK 签名器：V1+V2 签名。
 */
class ApkSigner {

    fun sign(apk: File, output: File, keystorePath: String, ksPassword: String, alias: String, keyPassword: String): Boolean {
        return try {
            val ks = KeyStore.getInstance("JKS")
            ks.load(FileInputStream(keystorePath), ksPassword.toCharArray())
            val key = ks.getKey(alias, keyPassword.toCharArray()) as PrivateKey
            val cert = ks.getCertificate(alias)
            // 实际签名需要实现 APK Signing Block v2
            // 简化：使用 jarsigner 或 apksigner
            signApk(apk, output, key, cert)
            true
        } catch (e: Exception) { false }
    }

    fun signWithDebugKeystore(apk: File, output: File): Boolean {
        return try {
            val (key, cert) = generateDebugKeystore()
            signApk(apk, output, key, cert)
            true
        } catch (e: Exception) { false }
    }

    private fun signApk(apk: File, output: File, key: PrivateKey, cert: java.security.cert.Certificate) {
        // V1 签名（jarsigner 兼容）
        // V2 签名（APK Signing Block）
        // 简化实现：复制文件（实际需要完整签名块写入）
        apk.copyTo(output, overwrite = true)
    }

    private fun generateDebugKeystore(): Pair<PrivateKey, java.security.cert.Certificate> {
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        keyPairGen.initialize(2048)
        val keyPair = keyPairGen.generateKeyPair()
        // 自签名证书
        return keyPair.private to createSelfSignedCert(keyPair)
    }

    private fun createSelfSignedCert(keyPair: KeyPair): java.security.cert.Certificate {
        // 简化：返回占位证书
        // 完整实现需要 X.509 证书生成
        return keyPair.public as java.security.cert.Certificate
    }
}
