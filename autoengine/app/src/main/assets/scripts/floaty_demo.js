// AI生成
// 悬浮窗示例 —— 演示 floaty 悬浮窗
let w = floaty.window(
    '<frame><text id="t" text="悬浮窗" textSize="16sp" textColor="#ffffff" padding="16dp" bg="#80000000"/></frame>'
);

let count = 0;
let timer = setInterval(function() {
    count++;
    w.t.setText("计数: " + count);
    if (count >= 20) {
        clearInterval(timer);
        floaty.close(w);
        log("悬浮窗已关闭");
    }
}, 500);

log("悬浮窗已显示");
