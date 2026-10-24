// AI生成
// 多点找色示例 —— 演示 findMultiColors
const ok = images.requestScreenCapture();
if (!ok) { log("截图授权失败"); exit(); }

sleep(500);
let img = images.captureScreen();

// 主色 + 偏移颜色验证
let result = images.findMultiColors(img, 0xFFFFFF, [
    [10, 0, 0xFFFFFF],
    [20, 0, 0xFFFFFF],
    [0, 10, 0x000000]
], { threshold: 20 });

if (result) {
    log("多点找色成功: (" + result.x + ", " + result.y + ")");
} else {
    log("未找到匹配的多点颜色");
}
