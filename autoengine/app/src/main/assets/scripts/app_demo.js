// AI生成
// App 启动示例 —— 演示 app API
log("已安装应用数量: " + app.getInstalledPackages().length);

// 启动设置
app.launch("com.android.settings");
log("已启动设置");

sleep(2000);

// 检查微信是否安装
if (app.isInstalled("com.tencent.mm")) {
    log("微信已安装，版本: " + app.getAppVersion("com.tencent.mm"));
} else {
    log("微信未安装");
}

// 打开网页
app.openUrl("https://www.baidu.com");
log("已打开百度");
