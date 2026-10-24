// AI生成
// VS Code 联动示例
"auto";

// 启动 DevPlugin 服务端，监听 VS Code 连接
let server = devplugin.createServer({
    name: "AutoJs Device",
    id: device.androidId,
    version: "1.0.0"
});

server.onConnected(function(client) {
    log("VS Code 已连接: " + client);
});

server.onCommand("run_script", function(data) {
    let scriptPath = data.script_path;
    log("收到运行脚本命令: " + scriptPath);
    let code = files.read(scriptPath);
    engines.execScript("vscode_task", code);
    return { success: true };
});

server.onCommand("save_script", function(data) {
    let path = data.path;
    let content = data.content;
    files.write(path, content);
    log("脚本已保存: " + path);
    return { success: true };
});

server.onCommand("list_files", function(data) {
    let dir = data.dir || "/sdcard/autojs/scripts/";
    let files = files.list(dir);
    return { files: files };
});

server.start(9317);
log("DevPlugin 服务已启动，端口 9317");
log("在 VS Code 中安装 AutoJs 插件即可连接");

// 广播日志到 VS Code
server.broadcastLog("设备已就绪", "info");

// 保持运行
setInterval(function() {
    server.broadcastLog("心跳: " + new Date().toLocaleTimeString(), "debug");
}, 30000);
