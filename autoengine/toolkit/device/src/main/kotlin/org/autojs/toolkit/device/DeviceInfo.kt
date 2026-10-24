package org.autojs.toolkit.device

import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.net.Uri
import android.view.WindowManager
import android.webkit.WebView
import android.content.Intent.*

/**
 * 设备信息工具：屏幕尺寸/密度、Build 信息、电池等。
 */
class DeviceInfo(private val context: Context) {

    val width: Int
        get() {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = android.util.DisplayMetrics()
            wm.defaultDisplay.getRealMetrics(metrics)
            return metrics.widthPixels
        }

    val height: Int
        get() {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = android.util.DisplayMetrics()
            wm.defaultDisplay.getRealMetrics(metrics)
            return metrics.heightPixels
        }

    val density: Float
        get() = context.resources.displayMetrics.density

    val densityDpi: Int
        get() = context.resources.displayMetrics.densityDpi

    val brand: String get() = Build.BRAND
    val model: String get() = Build.MODEL
    val manufacturer: String get() = Build.MANUFACTURER
    val sdkInt: Int get() = Build.VERSION.SDK_INT
    val release: String get() = Build.VERSION.RELEASE

    val battery: Int
        get() {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val battery = context.registerReceiver(null, filter)
            val level = battery?.getIntExtra("level", -1) ?: -1
            val scale = battery?.getIntExtra("scale", 100) ?: 100
            return (level * 100 / scale)
        }

    val isCharging: Boolean
        get() {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val battery = context.registerReceiver(null, filter)
            val status = battery?.getIntExtra("status", -1) ?: -1
            return status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                   status == android.os.BatteryManager.BATTERY_STATUS_FULL
        }

    val androidId: String
        get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""

    fun keepScreenOn(on: Boolean) {
        if (on) {
            context.getSystemService(Context.POWER_SERVICE)?.let {
                val pm = it as PowerManager
                // 需要权限 WAKE_LOCK
            }
        }
    }
}

/**
 * App 管理器：启动/卸载/查看应用。
 */
class AppManager(private val context: Context) {

    fun launch(packageName: String): Boolean = try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } else false
    } catch (e: Exception) { false }

    fun launchActivity(intent: Intent): Boolean = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent); true
    } catch (e: Exception) { false }

    fun uninstall(packageName: String): Boolean = try {
        val intent = Intent(ACTION_DELETE, Uri.parse("package:$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent); true
    } catch (e: Exception) { false }

    fun isInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0); true
    } catch (e: PackageManager.NameNotFoundException) { false }

    fun getAppName(packageName: String): String? = try {
        val pi = context.packageManager.getPackageInfo(packageName, 0)
        context.packageManager.getApplicationLabel(pi.applicationInfo!!).toString()
    } catch (e: Exception) { null }

    fun getAppVersion(packageName: String): String? = try {
        context.packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: Exception) { null }

    fun viewFile(path: String): Boolean = try {
        val intent = Intent(ACTION_VIEW).apply {
            setDataAndType(Uri.parse("file://$path"), "*/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent); true
    } catch (e: Exception) { false }

    fun openUrl(url: String): Boolean = try {
        val intent = Intent(ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent); true
    } catch (e: Exception) { false }

    fun getInstalledPackages(): List<String> {
        return context.packageManager.getInstalledApplications(0).map { it.packageName }
    }
}
