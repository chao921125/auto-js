// AI生成
// 截图保存示例 —— 演示截图与保存
const ok = images.requestScreenCapture();
if (!ok) { log("截图授权失败"); exit(); }

sleep(500);

let img = images.captureScreen();
if (img) {
    let path = "/sdcard/screenshot_" + Date.now() + ".png";
    images.save(img, path);
    log("截图已保存: " + path);

    // 裁剪区域
    let cropped = images.crop(img, [0, 0, 200, 200]);
    let cropPath = "/sdcard/cropped_" + Date.now() + ".png";
    images.save(cropped, cropPath);
    log("裁剪图已保存: " + cropPath);

    // 获取像素颜色
    let pixel = images.pixel(img, 100, 100);
    log("像素(100,100)颜色: #" + pixel.toString(16));
}
