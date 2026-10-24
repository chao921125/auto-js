package org.autojs.toolkit.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager

/**
 * 无障碍服务工具：检查服务状态、引导用户开启。
 */
object AccessibilityUtils {

    fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val serviceName = context.packageName + "/" + AutoJsAccessibilityService::class.java.name
        return enabledServices.contains(serviceName)
    }

    fun getServiceInfo(context: Context): AccessibilityServiceInfo? {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val serviceName = AutoJsAccessibilityService::class.java.name
        return am.installedAccessibilityServiceList.find {
            it.id.contains(serviceName)
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = android.content.Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
