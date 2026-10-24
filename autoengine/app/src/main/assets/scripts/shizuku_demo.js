// AI生成
// Shizuku 高权限操作示例
"auto";

// 检查 Shizuku 是否可用
if (!shizuku.isAvailable()) {
    log("Shizuku 未运行，请先安装并启动 Shizuku");
    exit();
}

log("Shizuku 已连接");

// 检查权限
if (!shizuku.checkPermission()) {
    log("请授予 Shizuku 权限");
    shizuku.init();
    exit();
}

shizuku.init();
log("Shizuku 权限已授予");

// 通过 Shizuku 执行高权限命令
let r = shizuku.exec("pm list packages -3");
log("第三方应用列表:");
log(r.output);

// 示例：通过 Shizuku 授予权限
// shizuku.exec("pm grant com.example.app android.permission.WRITE_SECURE_SETTINGS");

shizuku.destroy();
log("Shizuku 资源已释放");
