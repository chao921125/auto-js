package net.cc.cca.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * 主题管理器
 * 
 * 功能：
 * - 支持浅色/深色/跟随系统三种模式
 * - 持久化主题设置
 * - Android 10+ 系统主题跟随
 * 
 * @author CCA Team
 * @since 2026-05-19
 */
public class ThemeManager {
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    /**
     * 主题模式枚举
     */
    public enum ThemeMode {
        /** 浅色主题 */
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        
        /** 深色主题 */
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        
        /** 跟随系统（Android 10+） */
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        private final int mode;
        
        ThemeMode(int mode) {
            this.mode = mode;
        }
        
        public int getMode() {
            return mode;
        }
    }
    
    /**
     * 获取当前主题模式
     */
    public static ThemeMode getCurrentThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name());
        try {
            return ThemeMode.valueOf(modeName);
        } catch (IllegalArgumentException e) {
            return ThemeMode.SYSTEM;
        }
    }
    
    /**
     * 设置主题模式
     */
    public static void setThemeMode(Context context, ThemeMode mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME_MODE, mode.name()).apply();
        
        // 应用主题
        applyTheme(mode);
    }
    
    /**
     * 应用主题
     */
    public static void applyTheme(ThemeMode mode) {
        AppCompatDelegate.setDefaultNightMode(mode.getMode());
    }
    
    /**
     * 初始化主题（在 Application.onCreate 中调用）
     */
    public static void initTheme(Context context) {
        ThemeMode mode = getCurrentThemeMode(context);
        applyTheme(mode);
    }
    
    /**
     * 检查是否支持深色主题
     * Android 10+ 完全支持
     */
    public static boolean isDarkThemeSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
    
    /**
     * 获取系统是否处于深色模式
     */
    public static boolean isSystemInDarkMode(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int mode = context.getResources().getConfiguration().uiMode & 
                      android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }
}
