// AI生成
// 多线程示例 —— 演示 threads 和事件总线
let count = 0;

let t1 = threads.start(function() {
    for (let i = 0; i < 10; i++) {
        log("线程1: " + i);
        sleep(500);
    }
});

let t2 = threads.start(function() {
    for (let i = 0; i < 10; i++) {
        log("线程2: " + i);
        sleep(700);
    }
});

log("主线程等待子线程完成...");
