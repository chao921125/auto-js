// AI生成
// 手势绘制示例 —— 演示多点手势
"auto";

auto.waitFor();
sleep(500);

// 绘制圆形手势
let centerX = device.width / 2;
let centerY = device.height / 2;
let radius = 200;
let points = [];
for (let angle = 0; angle <= 360; angle += 10) {
    let rad = angle * Math.PI / 180;
    points.push([
        centerX + radius * Math.cos(rad),
        centerY + radius * Math.sin(rad)
    ]);
}
auto.gesture(2000, points);
log("已绘制圆形");
