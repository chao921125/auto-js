// AI生成
// 窗口监听示例 —— 演示窗口变化监听
"auto";

auto.waitFor();

events.on("window_change", function(info) {
    log("窗口变化: " + info);
});

log("窗口监听已启动，请在手机上操作...");
log("当前窗口数: " + auto.windows().length);

for (let i = 0; i < auto.windows().length; i++) {
    let w = auto.window(i);
    log("窗口 " + i + ": " + w.windowId + " (节点数: " + w.nodes.length + ")");
}
