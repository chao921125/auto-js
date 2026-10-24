// AI生成
// 控件遍历示例 —— 演示无障碍控件遍历
"auto";

auto.waitFor();
sleep(1000);

// 查找所有可点击的按钮
let buttons = auto.clickable().findAll();
log("找到 " + buttons.length + " 个可点击元素");

for (let i = 0; i < Math.min(buttons.length, 10); i++) {
    let btn = buttons[i];
    log("[" + (i+1) + "] text=" + (btn.text || "(无)") + " bounds=" + btn.bounds);
}

// 查找特定类名的控件
let textViews = auto.clazz("android.widget.TextView").findAll();
log("TextView 数量: " + textViews.length);
