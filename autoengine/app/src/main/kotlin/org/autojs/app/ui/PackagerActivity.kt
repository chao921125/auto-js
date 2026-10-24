package org.autojs.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.autojs.app.R
import org.autojs.app.script.ScriptManager
import org.autojs.feature.packager.ApkPackager
import java.io.File

class PackagerActivity : AppCompatActivity() {

    private lateinit var scriptPath: EditText
    private lateinit var packageName: EditText
    private lateinit var appName: EditText
    private lateinit var versionName: EditText
    private lateinit var btnPackage: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_packager)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "打包 APK"

        scriptPath = findViewById(R.id.et_script_path)
        packageName = findViewById(R.id.et_package_name)
        appName = findViewById(R.id.et_app_name)
        versionName = findViewById(R.id.et_version_name)
        btnPackage = findViewById(R.id.btn_package)
        progressBar = findViewById(R.id.progress_bar)
        statusText = findViewById(R.id.status_text)

        intent.getStringExtra("script_path")?.let { scriptPath.setText(it) }

        btnPackage.setOnClickListener { startPackaging() }
    }

    private fun startPackaging() {
        val config = ApkPackager.PackageConfig(
            mainScript = scriptPath.text.toString(),
            packageName = packageName.text.toString().ifBlank { "org.autojs.packaged" },
            appName = appName.text.toString().ifBlank { "AutoJs App" },
            versionName = versionName.text.toString().ifBlank { "1.0.0" }
        )
        progressBar.progress = 0
        statusText.text = "准备打包..."
        Thread {
            val templateApk = File(filesDir, "template.apk")
            val outputDir = filesDir
            val result = ApkPackager().packageApk(config, templateApk, outputDir) { progress, msg ->
                runOnUiThread {
                    progressBar.progress = progress
                    statusText.text = msg
                }
            }
            runOnUiThread {
                if (result.success) {
                    statusText.text = "打包成功: ${result.apkPath}"
                } else {
                    statusText.text = "打包失败: ${result.error}"
                }
            }
        }.start()
    }
}
