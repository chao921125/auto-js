// AI生成
// 微信签到示例 —— 无障碍 + 找图 + 日志
"auto";

auto.waitFor();
app.launch("com.tencent.mm");

let me = auto.text("我").findOne(8000);
me.parent().click();
log("已进入「我」页");

const ok = images.requestScreenCapture();
if (!ok) throw "截图授权失败";

let hit = null;
for (let i = 0; i < 30 && !hit; i++) {
    let img = images.captureScreen();
    hit = images.findImage(img, "sign_btn.png", {
        region: [0, device.height * 0.6, device.width, device.height * 0.4],
        threshold: 0.85
    });
    sleep(500);
}
if (hit) { auto.gesture(200, [hit.x, hit.y]); log("签到完成"); }
else { log("未找到签到按钮"); }
