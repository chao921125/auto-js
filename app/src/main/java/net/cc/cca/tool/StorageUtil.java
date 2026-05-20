package net.cc.cca.tool;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Android 10+ 存储适配工具类
 * 
 * 功能：
 * 1. 统一处理分区存储（Scoped Storage）
 * 2. 提供 MediaStore API 用于媒体文件访问
 * 3. 提供应用私有目录访问
 * 4. 兼容 MANAGE_EXTERNAL_STORAGE 权限
 * 
 * @author CCA Team
 * @since 2026-05-19
 */
public class StorageUtil {
    
    private static final String TAG = "StorageUtil";
    
    /**
     * 获取脚本默认存储目录
     * Android 10+ 优先使用应用私有目录
     * 
     * @param context Context
     * @return 脚本目录路径
     */
    public static String getScriptDirPath(Context context) {
        // 使用应用私有目录，无需权限
        File scriptDir = context.getExternalFilesDir("scripts");
        if (scriptDir != null) {
            return scriptDir.getAbsolutePath();
        }
        // fallback 到内部存储
        return new File(context.getFilesDir(), "scripts").getAbsolutePath();
    }
    
    /**
     * 获取外部存储公共目录（需要 MANAGE_EXTERNAL_STORAGE 权限）
     * 
     * @deprecated Android 10+ 不推荐使用，请使用 MediaStore 或 SAF
     * @return 外部存储根目录
     */
    @Deprecated
    public static String getLegacyExternalStoragePath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 如果没有 MANAGE_EXTERNAL_STORAGE 权限，返回 null
            if (!Environment.isExternalStorageManager()) {
                Log.w(TAG, "MANAGE_EXTERNAL_STORAGE permission not granted");
                return null;
            }
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
    
    /**
     * 保存图片到 MediaStore（Android 10+ 推荐方式）
     * 
     * @param context Context
     * @param inputStream 图片输入流
     * @param fileName 文件名（例如：screenshot_20260519.png）
     * @param mimeType MIME 类型（例如：image/png）
     * @return 保存成功的 Uri，失败返回 null
     */
    public static Uri saveImageToMediaStore(Context context, InputStream inputStream, 
                                            String fileName, String mimeType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 及以下使用传统方式
            return null; // 调用方需要自己处理
        }
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CCA");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);
        
        Uri uri = context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        
        if (uri == null) {
            Log.e(TAG, "Failed to insert image to MediaStore");
            return null;
        }
        
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                // 标记为非 pending 状态
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                context.getContentResolver().update(uri, values, null, null);
                
                Log.i(TAG, "Image saved to MediaStore: " + uri);
                return uri;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save image to MediaStore", e);
            // 清理失败的记录
            context.getContentResolver().delete(uri, null, null);
        }
        
        return null;
    }
    
    /**
     * 获取应用的缓存目录
     * 
     * @param context Context
     * @return 缓存目录路径
     */
    public static String getCacheDir(Context context) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            return cacheDir.getAbsolutePath();
        }
        return context.getCacheDir().getAbsolutePath();
    }
    
    /**
     * 获取日志文件存储目录
     * 
     * @param context Context
     * @return 日志目录路径
     */
    public static String getLogDir(Context context) {
        File logDir = new File(context.getExternalFilesDir("logs"), "cca");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        return logDir.getAbsolutePath();
    }
    
    /**
     * 获取项目构建输出目录（使用 SAF 选择）
     * 
     * @param context Context
     * @return 默认输出目录（应用私有）
     */
    public static String getBuildOutputDir(Context context) {
        File outputDir = context.getExternalFilesDir("builds");
        if (outputDir != null && outputDir.exists()) {
            return outputDir.getAbsolutePath();
        }
        return new File(context.getFilesDir(), "builds").getAbsolutePath();
    }
    
    /**
     * 检查是否有外部存储访问权限
     * 
     * @return true 如果有 MANAGE_EXTERNAL_STORAGE 权限或 Android 9 及以下
     */
    public static boolean hasExternalStorageAccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true; // Android 9 及以下不需要特殊权限
        }
        return Environment.isExternalStorageManager();
    }
    
    /**
     * 简化路径显示（移除应用私有目录前缀）
     * 
     * @param path 完整路径
     * @param context Context
     * @return 简化后的路径
     */
    public static String getSimplifiedPath(String path, Context context) {
        if (path == null) return "";
        
        String privatePath = context.getExternalFilesDir(null).getAbsolutePath();
        if (path.startsWith(privatePath)) {
            return path.substring(privatePath.length());
        }
        
        // 如果是传统 SD 卡路径，简化显示
        String legacyPath = getLegacyExternalStoragePath();
        if (legacyPath != null && path.startsWith(legacyPath)) {
            return path.substring(legacyPath.length());
        }
        
        return path;
    }
}
