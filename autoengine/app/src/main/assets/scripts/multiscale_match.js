// AI生成
// 模板匹配进阶 —— 多尺度找图
const ok = images.requestScreenCapture();
if (!ok) { log("截图授权失败"); exit(); }

sleep(500);

let template = images.read("template.png");
if (!template) { log("模板图片不存在"); exit(); }

let img = images.captureScreen();

// 多尺度匹配
let scales = [0.8, 0.9, 1.0, 1.1, 1.2];
let bestMatch = null;

for (let s of scales) {
    let scaled = images.resize(template, template.width * s, template.height * s);
    let match = images.findImage(img, scaled, { threshold: 0.75 });
    if (match && (!bestMatch || match.confidence > bestMatch.confidence)) {
        bestMatch = match;
    }
}

if (bestMatch) {
    log("最佳匹配: (" + bestMatch.x + "," + bestMatch.y + ") 置信度:" + bestMatch.confidence.toFixed(3));
    auto.click(bestMatch.x + bestMatch.width/2, bestMatch.y + bestMatch.height/2);
} else {
    log("所有尺度均未匹配");
}
