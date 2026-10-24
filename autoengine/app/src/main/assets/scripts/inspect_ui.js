// AI生成
// 控件属性检查示例
"auto";

auto.waitFor();
sleep(1000);

let allNodes = auto.clazz("android.widget.TextView").findAll();
log("TextView 总数: " + allNodes.length);

for (let i = 0; i < Math.min(allNodes.length, 5); i++) {
    let n = allNodes[i];
    log("--- 节点 " + (i+1) + " ---");
    log("text: " + (n.text || "(空)"));
    log("id: " + (n.resourceId || "(无)"));
    log("desc: " + (n.desc || "(无)"));
    log("class: " + (n.className || "(无)"));
    log("bounds: " + n.bounds);
    log("depth: " + n.depth);
    log("clickable: " + n.isClickable);
    log("enabled: " + n.isEnabled);
}
