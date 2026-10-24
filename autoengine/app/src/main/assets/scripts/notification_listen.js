// AI生成
// 通知监听示例
"auto";

// 监听通知
notifications.on("posted", function(n) {
    log("收到通知: " + n.title + " - " + n.text);
    log("来源: " + n.packageName);

    // 示例：微信消息自动回复
    if (n.packageName === "com.tencent.mm" && n.text.contains("验证码")) {
        log("检测到验证码消息");
        // 提取验证码
        let match = n.text.match(/\d{4,6}/);
        if (match) {
            log("验证码: " + match[0]);
        }
    }
});

notifications.on("removed", function(n) {
    log("通知被移除: " + n.title);
});

log("通知监听已启动，请在系统设置中开启通知访问权限");
log("当前监听状态: " + notifications.isRunning());

// 获取当前所有活跃通知
let active = notifications.getActive();
log("当前活跃通知数: " + active.length);
