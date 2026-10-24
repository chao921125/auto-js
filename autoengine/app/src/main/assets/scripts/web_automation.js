// AI生成
// 网页自动化示例 —— 打开浏览器并操作
"auto";

app.openUrl("https://www.baidu.com");
sleep(3000);

let searchBox = auto.id("index-kw").findOne(5000) || auto.clazz("android.widget.EditText").findOne(3000);
if (searchBox) {
    searchBox.setText("AutoJs 自动化");
    sleep(500);
    auto.text("百度一下").findOne(3000)?.click();
    log("已搜索");
} else {
    log("未找到搜索框");
}
