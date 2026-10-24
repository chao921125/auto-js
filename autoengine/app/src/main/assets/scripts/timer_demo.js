// AI生成
// 定时器示例 —— 演示 setTimeout / setInterval
log("启动定时器...");

let count = 0;
let timer = setInterval(function() {
    count++;
    log("定时器触发 #" + count);
    if (count >= 5) {
        clearInterval(timer);
        log("定时器已停止");
    }
}, 1000);

setTimeout(function() {
    log("一次性定时器触发");
}, 3000);
