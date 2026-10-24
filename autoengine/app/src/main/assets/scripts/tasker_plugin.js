// AI生成
// Tasker 插件集成示例
"auto";

// 此脚本由 Tasker 触发执行
// Tasker 传入的变量以 %var_name 形式可用

log("Tasker 触发脚本启动");

// 从 Tasker 全局变量获取配置
let targetApp = global("TARGET_APP") || "com.tencent.mm";
let action = global("ACTION") || "open";

log("目标应用: " + targetApp);
log("执行动作: " + action);

if (action === "open") {
    app.launch(targetApp);
    sleep(3000);
    log("应用已启动");
} else if (action === "close") {
    shell.exec("am force-stop " + targetApp, false);
    log("应用已关闭");
} else if (action === "clear") {
    shell.exec("pm clear " + targetApp, false);
    log("应用数据已清除");
}

// 将结果回传给 Tasker
setGlobal("SCRIPT_RESULT", "success");
setGlobal("SCRIPT_TIME", new Date().toLocaleTimeString());

log("Tasker 脚本执行完成");
