// AI生成
// 长按示例 —— 演示长按操作
"auto";

auto.waitFor();

let item = auto.text("列表项").findOne(5000);
if (item) {
    item.longClick();
    log("已长按列表项");
    sleep(500);

    let deleteBtn = auto.text("删除").findOne(2000);
    if (deleteBtn) {
        deleteBtn.click();
        log("已点击删除");
    }
} else {
    log("未找到列表项");
}
