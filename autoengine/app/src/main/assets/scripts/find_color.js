// AI生成
// 找色示例 —— 演示屏幕找色
"auto";

const ok = images.requestScreenCapture();
if (!ok) { log("截图授权失败"); exit(); }

let img = images.captureScreen();
let result = images.findColor(img, 0xFF6F00, {
    region: [0, 0, device.width, device.height],
    threshold: 10
});

if (result) {
    log("找到颜色，位置: " + result.x + ", " + result.y);
    auto.click(result.x, result.y);
} else {
    log("未找到指定颜色");
}
