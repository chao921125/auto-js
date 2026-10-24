// AI生成
// 支付宝签到示例
"auto";

auto.waitFor();
app.launch("com.eg.android.AlipayGphone");
sleep(5000);

// 点击「首页」tab
auto.text("首页").findOne(5000)?.click();
sleep(1000);

// 点击「蚂蚁森林」
auto.text("蚂蚁森林").findOne(5000)?.click();
sleep(3000);

log("已进入蚂蚁森林");
// 收能量逻辑...
auto.textContains("收集能量").findOne(3000)?.click();
log("签到完成");
