// AI生成
// 滑动操作示例 —— 演示手势滑动
"auto";

auto.waitFor();

// 向上滑动
auto.swipe(device.width / 2, device.height * 0.8, device.width / 2, device.height * 0.2, 500);
log("已向上滑动");

sleep(1000);

// 向下滑动
auto.swipe(device.width / 2, device.height * 0.2, device.width / 2, device.height * 0.8, 500);
log("已向下滑动");
