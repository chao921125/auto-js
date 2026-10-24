// AI生成
// 布局分析器示例
"auto";
auto.waitFor();

// 等待 3 秒后分析当前界面
log("3 秒后分析当前界面...");
sleep(3000);

// 获取当前界面的布局树
let root = inspector.inspect();
if (!root) {
    log("无法获取布局，请确保无障碍服务已开启");
    exit();
}

log("当前窗口布局:");
log("根节点: " + root.className);

// 遍历并打印所有可点击控件
function printClickable(node, depth) {
    if (node.clickable) {
        let indent = "  ".repeat(depth);
        let text = node.text ? " text=\"" + node.text + "\"" : "";
        let id = node.id ? " id=" + node.id : "";
        log(indent + "▶ " + node.className + text + id + " bounds=" + node.bounds);
    }
    for (let i = 0; i < node.children.length; i++) {
        printClickable(node.children[i], depth + 1);
    }
}

printClickable(root, 0);

// 生成选择器代码
log("\n可用的选择器代码:");
if (root.clickable) {
    log(root.toSelectorCode());
}
for (let i = 0; i < root.children.length; i++) {
    if (root.children[i].clickable) {
        log(root.children[i].toSelectorCode());
    }
}
