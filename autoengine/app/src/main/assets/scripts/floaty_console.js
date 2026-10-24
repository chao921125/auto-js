// AI生成
// 悬浮控制台示例 —— 演示 floaty.console
let console = floaty.console();
console.show();
console.log("I", "悬浮控制台已启动");

for (let i = 0; i < 10; i++) {
    console.log("I", "日志 #" + i);
    sleep(500);
}

console.log("W", "这是一条警告");
console.log("E", "这是一条错误");
sleep(2000);
console.hide();
log("悬浮控制台已关闭");
