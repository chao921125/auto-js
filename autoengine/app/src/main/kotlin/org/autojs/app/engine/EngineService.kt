package org.autojs.app.engine

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.autojs.app.MainActivity
import org.autojs.app.R

/**
 * 脚本引擎前台服务：承载脚本运行，保活通知。
 */
class EngineService : Service() {

    companion object {
        const val CHANNEL_ID = "engine_service"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_SCRIPT = "START_SCRIPT"
        const val ACTION_STOP_SCRIPT = "STOP_SCRIPT"
        const val ACTION_STOP_ALL = "STOP_ALL"
        const val EXTRA_SCRIPT_PATH = "script_path"

        fun startScript(context: Context, scriptPath: String) {
            val intent = Intent(context, EngineService::class.java).apply {
                action = ACTION_START_SCRIPT
                putExtra(EXTRA_SCRIPT_PATH, scriptPath)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAll(context: Context) {
            val intent = Intent(context, EngineService::class.java).apply { action = ACTION_STOP_ALL }
            context.startService(intent)
        }
    }

    private val engineManager get() = EngineManager.get(this)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("AutoJs 引擎运行中"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SCRIPT -> {
                val path = intent.getStringExtra(EXTRA_SCRIPT_PATH) ?: return START_NOT_STICKY
                val runtimeId = engineManager.executeScript(path)
                engineManager.getRuntime(runtimeId)?.onLog = { level, msg ->
                    LogEntryBus.emit(level, msg)
                }
                updateNotification("正在运行: ${java.io.File(path).name}")
            }
            ACTION_STOP_SCRIPT -> {
                intent.getStringExtra("runtime_id")?.let { engineManager.stopScript(it) }
            }
            ACTION_STOP_ALL -> {
                engineManager.stopAll()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        engineManager.stopAll()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "脚本引擎", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "脚本运行状态通知"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 1, Intent(this, EngineService::class.java).apply { action = ACTION_STOP_ALL },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoJs Pro")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(0, "停止全部", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(text))
    }
}

/**
 * 日志事件总线：跨组件传递日志。
 */
object LogEntryBus {
    private val listeners = mutableListOf<(String, String) -> Unit>()

    fun onLog(listener: (level: String, msg: String) -> Unit) {
        listeners.add(listener)
    }

    fun offLog(listener: (String, String) -> Unit) {
        listeners.remove(listener)
    }

    fun emit(level: String, msg: String) {
        listeners.forEach { it(level, msg) }
    }
}
