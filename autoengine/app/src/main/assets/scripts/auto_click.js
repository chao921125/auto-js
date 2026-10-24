// AI生成
// 自动点击示例 —— 演示无障碍控件查找与点击
"auto";

auto.waitFor();

// 查找包含"确定"的按钮并点击
let btn = auto.text("确定").findOne(5000);
if (btn) {
    btn.click();
    log("已点击确定按钮");
} else {
    log("未找到确定按钮");
}
