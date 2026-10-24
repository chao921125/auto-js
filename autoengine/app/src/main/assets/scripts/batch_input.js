// AI生成
// 批量输入示例 —— 演示文本输入
"auto";

auto.waitFor();

let inputs = ["张三", "李四", "王五", "赵六", "钱七"];

for (let i = 0; i < inputs.length; i++) {
    let field = auto.clazz("android.widget.EditText").findOne(3000);
    if (field) {
        field.setText(inputs[i]);
        log("已输入: " + inputs[i]);
        sleep(500);

        let submit = auto.text("提交").findOne(2000);
        if (submit) submit.click();
        sleep(1000);
    }
}
log("批量输入完成");
