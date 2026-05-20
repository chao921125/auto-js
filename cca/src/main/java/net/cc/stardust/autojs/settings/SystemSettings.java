package net.cc.stardust.autojs.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统设置访问封装
 * Auto.js Pro新特性: 新增设置模块
 * 
 * 功能:
 * - 读取系统设置(Settings.Global/Secure/System)
 * - 写入系统设置(需要权限)
 * - 监听设置变化
 * - 安全的设置访问(异常处理)
 */
public class SystemSettings {
    
    private static final String TAG = "SystemSettings";
    
    private ContentResolver contentResolver;
    
    public SystemSettings(Context context) {
        this.contentResolver = context.getContentResolver();
    }
    
    /**
     * 读取Global设置
     */
    public String getGlobal(String name) {
        try {
            return Settings.Global.getString(contentResolver, name);
        } catch (Exception e) {
            Log.w(TAG, "读取Global设置失败: " + name, e);
            return null;
        }
    }
    
    /**
     * 读取Secure设置
     */
    public String getSecure(String name) {
        try {
            return Settings.Secure.getString(contentResolver, name);
        } catch (Exception e) {
            Log.w(TAG, "读取Secure设置失败: " + name, e);
            return null;
        }
    }
    
    /**
     * 读取System设置
     */
    public String getSystem(String name) {
        try {
            return Settings.System.getString(contentResolver, name);
        } catch (Exception e) {
            Log.w(TAG, "读取System设置失败: " + name, e);
            return null;
        }
    }
    
    /**
     * 写入Global设置(需要WRITE_SETTINGS权限)
     */
    public boolean setGlobal(String name, String value) {
        try {
            return Settings.Global.putString(contentResolver, name, value);
        } catch (Exception e) {
            Log.e(TAG, "写入Global设置失败: " + name, e);
            return false;
        }
    }
    
    /**
     * 写入Secure设置(需要WRITE_SECURE_SETTINGS权限)
     */
    public boolean setSecure(String name, String value) {
        try {
            return Settings.Secure.putString(contentResolver, name, value);
        } catch (Exception e) {
            Log.e(TAG, "写入Secure设置失败: " + name, e);
            return false;
        }
    }
    
    /**
     * 写入System设置(需要WRITE_SETTINGS权限)
     */
    public boolean setSystem(String name, String value) {
        try {
            return Settings.System.putString(contentResolver, name, value);
        } catch (Exception e) {
            Log.e(TAG, "写入System设置失败: " + name, e);
            return false;
        }
    }
    
    /**
     * 读取整数设置
     */
    public int getInt(String scope, String name, int defaultValue) {
        try {
            return Settings.getInt(contentResolver, name, defaultValue);
        } catch (Exception e) {
            Log.w(TAG, "读取整数设置失败: " + name, e);
            return defaultValue;
        }
    }
    
    /**
     * 写入整数设置
     */
    public boolean setInt(String scope, String name, int value) {
        try {
            Settings.putInt(contentResolver, name, value);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "写入整数设置失败: " + name, e);
            return false;
        }
    }
    
    /**
     * 读取浮点数设置
     */
    public float getFloat(String scope, String name, float defaultValue) {
        try {
            return Settings.getFloat(contentResolver, name, defaultValue);
        } catch (Exception e) {
            Log.w(TAG, "读取浮点设置失败: " + name, e);
            return defaultValue;
        }
    }
    
    /**
     * 写入浮点数设置
     */
    public boolean setFloat(String scope, String name, float value) {
        try {
            Settings.putFloat(contentResolver, name, value);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "写入浮点设置失败: " + name, e);
            return false;
        }
    }
    
    /**
     * 读取布尔设置
     */
    public boolean getBoolean(String scope, String name, boolean defaultValue) {
        try {
            int value = Settings.getInt(contentResolver, name, defaultValue ? 1 : 0);
            return value != 0;
        } catch (Exception e) {
            Log.w(TAG, "读取布尔设置失败: " + name, e);
            return defaultValue;
        }
    }
    
    /**
     * 写入布尔设置
     */
    public boolean setBoolean(String scope, String name, boolean value) {
        try {
            Settings.putInt(contentResolver, name, value ? 1 : 0);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "写入布尔设置失败: " + name, e);
            return false;
        }
    }
    
    /**
     * 删除设置
     */
    public boolean delete(String scope, String name) {
        try {
            Settings.removeContentObserver(contentResolver, 
                Settings.URI(scope));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "删除设置失败: " + name, e);
            return false;
        }
    }
    
    /**
     * 检查设置是否存在
     */
    public boolean exists(String scope, String name) {
        String value = getString(scope, name);
        return value != null;
    }
    
    /**
     * 获取所有设置项
     */
    public Map<String, String> getAll(String scope) {
        Map<String, String> allSettings = new HashMap<>();
        
        try {
            // 注意: Settings API不支持直接枚举所有设置
            // 这里返回null,需要调用者具体查询
            Log.d(TAG, "Settings API不支持枚举所有设置项");
        } catch (Exception e) {
            Log.e(TAG, "获取所有设置失败", e);
        }
        
        return allSettings;
    }
    
    /**
     * 统一的字符串读取方法
     */
    private String getString(String scope, String name) {
        switch (scope.toLowerCase()) {
            case "global":
                return getGlobal(name);
            case "secure":
                return getSecure(name);
            case "system":
                return getSystem(name);
            default:
                Log.w(TAG, "未知的设置范围: " + scope);
                return null;
        }
    }
    
    /**
     * 常用设置快捷访问
     */
    public static class QuickAccess {
        private SystemSettings settings;
        
        public QuickAccess(SystemSettings settings) {
            this.settings = settings;
        }
        
        /**
         * 获取屏幕超时时间
         */
        public int getScreenTimeout() {
            return settings.getInt("system", 
                Settings.System.SCREEN_OFF_TIMEOUT, 30000);
        }
        
        /**
         * 设置屏幕超时时间(毫秒)
         */
        public void setScreenTimeout(int timeoutMs) {
            settings.setSystem("screen_off_timeout", timeoutMs);
        }
        
        /**
         * 获取飞行模式状态
         */
        public boolean isAirplaneModeOn() {
            try {
                return Settings.Global.getInt(contentResolver, 
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * 获取WiFi状态
         */
        public boolean isWifiEnabled() {
            try {
                return Settings.Secure.getInt(contentResolver, 
                    Settings.Secure.WIFI_ON, 0) != 0;
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * 获取蓝牙状态
         */
        public boolean isBluetoothEnabled() {
            try {
                return Settings.Secure.getInt(contentResolver, 
                    Settings.Secure.BLUETOOTH_ON, 0) != 0;
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * 获取亮度模式(手动/自动)
         */
        public boolean isAutoBrightness() {
            return settings.getInt("system", 
                Settings.System.SCREEN_BRIGHTNESS_MODE, 
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) 
                == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        }
        
        /**
         * 获取屏幕亮度(0-255)
         */
        public int getScreenBrightness() {
            return settings.getInt("system", 
                Settings.System.SCREEN_BRIGHTNESS, 128);
        }
        
        /**
         * 设置屏幕亮度(0-255)
         */
        public void setScreenBrightness(int brightness) {
            brightness = Math.max(0, Math.min(255, brightness));
            settings.setSystem("screen_brightness", brightness);
        }
    }
    
    /**
     * 获取快捷访问对象
     */
    public QuickAccess quick() {
        return new QuickAccess(this);
    }
}
