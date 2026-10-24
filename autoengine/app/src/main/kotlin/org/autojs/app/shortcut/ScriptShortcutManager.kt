package org.autojs.app.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

/**
 * 脚本快捷方式管理：为脚本创建桌面快捷方式。
 *
 * 替代旧版 app/external/shortcut/ShortcutManager.java，
 * 使用 ShortcutManager API（API 25+，新版 minSdk=24 兼容）。
 */
class ScriptShortcutManager(private val context: Context) {

    companion object {
        const val ACTION_RUN_SCRIPT = "org.autojs.app.action.RUN_SCRIPT"
        const val EXTRA_SCRIPT_PATH = "script_path"
    }

    /**
     * 为脚本创建桌面快捷方式。
     */
    fun createShortcut(scriptPath: String, scriptName: String): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) return false

        val intent = Intent(context, org.autojs.app.MainActivity::class.java).apply {
            action = ACTION_RUN_SCRIPT
            putExtra(EXTRA_SCRIPT_PATH, scriptPath)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcut = ShortcutInfoCompat.Builder(context, "script_${scriptPath.hashCode()}")
            .setShortLabel(scriptName)
            .setLongLabel("运行 $scriptName")
            .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_media_play))
            .setIntent(intent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    /**
     * 获取所有动态快捷方式。
     */
    fun getShortcuts(): List<ShortcutInfoCompat> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManagerCompat.getDynamicShortcuts(context)
        } else {
            emptyList()
        }

    /**
     * 移除快捷方式。
     */
    fun removeShortcut(shortcutId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcutId))
        }
    }
}
