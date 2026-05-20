package net.cc.stardust.core.image.capture;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import net.cc.cca.R;

/**
 * 媒体投射通知管理器
 * 
 * Android 10+ 强制要求：
 * - 进行屏幕投射时必须显示持久通知
 * - 通知必须包含停止投射的操作按钮
 * 
 * @author CCA Team
 * @since 2026-05-19
 */
public class MediaProjectionNotificationManager {
    
    private static final String CHANNEL_ID = "media_projection_channel";
    private static final int NOTIFICATION_ID = 10001;
    
    private final Context context;
    private final NotificationManager notificationManager;
    
    public MediaProjectionNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    /**
     * 创建通知渠道（Android 8.0+）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "屏幕投射",
                NotificationManager.IMPORTANCE_LOW
        );
        
        channel.setDescription("正在进行的屏幕录制或投射");
        channel.setShowBadge(false);
        channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        
        notificationManager.createNotificationChannel(channel);
    }
    
    /**
     * 显示投射通知
     * 
     * @param stopIntent 停止投射的 PendingIntent
     */
    public void showProjectionNotification(PendingIntent stopIntent) {
        createNotificationChannel();
        
        // 创建停止操作按钮
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel,
                "停止投射",
                stopIntent
        ).build();
        
        // 构建通知
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("屏幕投射中")
                .setContentText("CCA 正在进行屏幕投射")
                .setSmallIcon(android.R.drawable.ic_menu_gallery)
                .setOngoing(true)  // 设置为持续通知
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(stopAction)
                .build();
        
        // 显示通知
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    /**
     * 隐藏投射通知
     */
    public void hideProjectionNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
    
    /**
     * 构建前台服务通知
     * 
     * 用于 MediaProjection 前台服务
     * Android 10+ 要求前台服务必须显示通知
     */
    public Notification buildForegroundServiceNotification(PendingIntent stopIntent) {
        createNotificationChannel();
        
        // 创建停止操作按钮
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel,
                "停止",
                stopIntent
        ).build();
        
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("屏幕投射服务")
                .setContentText("CCA 屏幕投射服务正在运行")
                .setSmallIcon(android.R.drawable.ic_menu_gallery)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(stopAction)
                .build();
    }
    
    /**
     * 获取通知 ID
     */
    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }
}
