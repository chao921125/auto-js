// AI生成
// 等待控件示例 —— 演示 waitFor 和 exists
"auto";

auto.waitFor();

// 等待特定文本出现
log("等待「登录」按钮...");
let loginBtn = auto.text("登录").waitFor(10000);
if (loginBtn) {
    log("找到登录按钮，准备点击");
    loginBtn.click();
} else {
    log("10秒内未找到登录按钮");
}

// 检查控件是否存在
if (auto.text("确定").exists()) {
    log("确定按钮存在");
}
