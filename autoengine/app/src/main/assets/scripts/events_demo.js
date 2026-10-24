// AI生成
// 事件监听示例 —— 演示 events 事件总线
events.on("custom_event", function(data) {
    log("收到事件: " + JSON.stringify(data));
});

setTimeout(function() {
    events.emit("custom_event", {msg: "Hello", time: Date.now()});
}, 1000);

setTimeout(function() {
    events.emit("custom_event", {msg: "World", time: Date.now()});
}, 2000);

log("事件监听已注册");
