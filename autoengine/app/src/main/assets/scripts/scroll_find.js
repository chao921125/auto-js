// AI生成
// 循环滚动查找示例
"auto";

auto.waitFor();

function scrollAndFind(text, maxScrolls) {
    for (let i = 0; i < maxScrolls; i++) {
        let target = auto.text(text).findOne(1000);
        if (target) return target;
        auto.scrollDown();
        sleep(500);
    }
    return null;
}

let result = scrollAndFind("目标项", 10);
if (result) {
    log("找到目标项");
    result.click();
} else {
    log("滚动10次未找到");
}
