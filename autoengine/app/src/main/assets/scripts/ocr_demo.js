// AI生成
// OCR 文字识别示例 —— 演示 OCR API
"auto";

const ok = images.requestScreenCapture();
if (!ok) { log("截图授权失败"); exit(); }

sleep(1000);
let img = images.captureScreen();
let results = ocr.recognize(img);

log("识别到 " + results.items.length + " 个文本块:");
for (let i = 0; i < results.items.length; i++) {
    let item = results.items[i];
    log("[" + (i+1) + "] " + item.text + " @ (" + item.bounds.left + "," + item.bounds.top + ")");
}
