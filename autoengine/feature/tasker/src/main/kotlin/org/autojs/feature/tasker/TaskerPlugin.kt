package org.autojs.feature.tasker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson

/**
 * Tasker 插件集成：允许 Tasker 调用 AutoJs 执行脚本。
 *
 * 替代旧版 app/external/tasker/ 下的 TaskerScriptEditActivity/FireSettingReceiver，
 * 使用现代 Tasker 插件协议。
 */
class TaskerPlugin(private val context: Context) {

    companion object {
        const val EXTRA_SCRIPT_PATH = "net.dinglisch.android.tasker.extras.SCRIPT_PATH"
        const val EXTRA_SCRIPT_CONTENT = "net.dinglisch.android.tasker.extras.SCRIPT_CONTENT"
        const val EXTRA_VARIABLE_PREFIX = "net.dinglisch.android.tasker.extras.VARIABLE_PREFIX"
        const val BUNDLE_EXTRA_SCRIPT = "org.autojs.tasker.script"
        const val BUNDLE_EXTRA_CONFIG = "org.autojs.tasker.config"
        const val TASKER_BUNDLE_EXTRA = "com.twofortyfouram.locale.intent.extra.BUNDLE"
        const val TASKER_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.STRING_BLURB"
        const val TASKER_STRING_ENCRYPTED_BLURB = "com.twofortyfouram.locale.intent.extra.STRING_ENCRYPTED_BLURB"
        const val TASKER_ACTION_EDIT_SETTING = "com.twofortyfouram.locale.intent.action.EDIT_SETTING"
    }

    data class TaskerConfig(
        val scriptPath: String? = null,
        val scriptContent: String? = null,
        val variables: Map<String, String> = emptyMap(),
        val timeout: Long = 30000
    )

    data class TaskerResult(
        val success: Boolean,
        val output: String,
        val variables: Map<String, String> = emptyMap()
    )

    private val gson = Gson()

    /**
     * 从 Tasker Intent 中解析配置。
     */
    fun parseConfig(intent: Intent): TaskerConfig {
        val bundle = intent.getBundleExtra(TASKER_BUNDLE_EXTRA) ?: Bundle()
        val scriptPath = bundle.getString(BUNDLE_EXTRA_SCRIPT)
        val configJson = bundle.getString(BUNDLE_EXTRA_CONFIG)
        val config = if (configJson != null) {
            try { gson.fromJson(configJson, TaskerConfig::class.java) } catch (e: Exception) { null }
        } else null
        return config ?: TaskerConfig(scriptPath = scriptPath)
    }

    /**
     * 构建回传给 Tasker 的结果 Intent。
     */
    fun buildResultIntent(result: TaskerResult): Intent {
        val intent = Intent()
        val bundle = Bundle().apply {
            putString(TASKER_STRING_BLURB,
                if (result.success) "Script executed successfully" else "Script execution failed")
            putString(TASKER_STRING_ENCRYPTED_BLURB,
                gson.toJson(result))
        }
        intent.putExtra(TASKER_BUNDLE_EXTRA, bundle)
        if (result.variables.isNotEmpty()) {
            val varBundle = Bundle()
            result.variables.forEach { (k, v) -> varBundle.putString(k, v) }
            intent.putExtra("net.dinglisch.android.tasker.extras.VARIABLES", varBundle)
        }
        return intent
    }

    /**
     * 构建编辑配置页的 Intent。
     */
    fun buildEditIntent(config: TaskerConfig): Intent {
        val intent = Intent(TASKER_ACTION_EDIT_SETTING)
        val bundle = Bundle().apply {
            putString(BUNDLE_EXTRA_SCRIPT, config.scriptPath)
            putString(BUNDLE_EXTRA_CONFIG, gson.toJson(config))
        }
        intent.putExtra(TASKER_BUNDLE_EXTRA, bundle)
        return intent
    }
}
