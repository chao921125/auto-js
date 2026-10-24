package org.autojs.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * 脚本桌面小部件：一键运行指定脚本。
 *
 * 替代旧版 app/external/widget/ScriptWidget.java，
 * 使用现代 AppWidgetProvider API。
 */
class ScriptWidget : AppWidgetProvider() {

    companion object {
        const val EXTRA_SCRIPT_PATH = "script_path"
        const val ACTION_RUN_SCRIPT = "org.autojs.app.action.RUN_SCRIPT"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
            views.setTextViewText(android.R.id.text1, "▶ 运行脚本")
            val intent = Intent(ACTION_RUN_SCRIPT).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_SCRIPT_PATH, ScriptWidgetConfig.getScriptPath(context, id))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pi = PendingIntent.getActivity(
                context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(android.R.id.text1, pi)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
