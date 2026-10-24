package org.autojs.app.widget

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * 桌面小部件配置存储：记录每个 widget 对应的脚本路径。
 */
object ScriptWidgetConfig {
    private val kv by lazy { MMKV.mmkvWithID("script_widget") }

    fun getScriptPath(context: Context, widgetId: Int): String = kv.decodeString("widget_$widgetId", "") ?: ""

    fun setScriptPath(widgetId: Int, path: String) { kv.encode("widget_$widgetId", path) }

    fun remove(widgetId: Int) { kv.removeValueForKey("widget_$widgetId") }
}
