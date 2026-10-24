// AI生成
// Shell 命令示例 —— 演示 shell 执行
let result = shell.exec("ls -la /sdcard/");
log("退出码: " + result.code);
log("输出:\n" + result.result);

if (result.error) {
    log("错误: " + result.error);
}

log("Root 可用: " + shell.isRootAvailable());
