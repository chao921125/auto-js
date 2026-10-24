package org.autojs.toolkit.device

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope

/**
 * device 命名空间 API：设备信息。
 */
class DeviceApiProvider(private val context: android.content.Context) : ApiProvider {
    override val name = "device"
    private val info = DeviceInfo(context)

    override fun install(scope: ScriptScope) {
        scope.defineObject("device") {
            property("width", info.width)
            property("height", info.height)
            property("density", info.density)
            property("densityDpi", info.densityDpi)
            property("brand", info.brand)
            property("model", info.model)
            property("manufacturer", info.manufacturer)
            property("sdkInt", info.sdkInt)
            property("release", info.release)
            property("battery", info.battery)
            property("isCharging", info.isCharging)
            property("androidId", info.androidId)
            function("keepScreenOn") { args -> info.keepScreenOn(args[0] as? Boolean ?: true); null }
        }
    }
}

/**
 * app 命名空间 API：应用管理。
 */
class AppApiProvider(private val context: android.content.Context) : ApiProvider {
    override val name = "app"
    private val mgr = AppManager(context)

    override fun install(scope: ScriptScope) {
        scope.defineObject("app") {
            function("launch") { args -> mgr.launch(args[0]?.toString() ?: "") }
            function("launchActivity") { args -> mgr.launchActivity(args[0] as android.content.Intent) }
            function("uninstall") { args -> mgr.uninstall(args[0]?.toString() ?: "") }
            function("isInstalled") { args -> mgr.isInstalled(args[0]?.toString() ?: "") }
            function("getAppName") { args -> mgr.getAppName(args[0]?.toString() ?: "") }
            function("getAppVersion") { args -> mgr.getAppVersion(args[0]?.toString() ?: "") }
            function("viewFile") { args -> mgr.viewFile(args[0]?.toString() ?: "") }
            function("openUrl") { args -> mgr.openUrl(args[0]?.toString() ?: "") }
            function("getInstalledPackages") { _ -> mgr.getInstalledPackages() }
        }
    }
}
