// AI生成
// 文件操作示例 —— 演示文件读写
files.write("test.txt", "Hello AutoJs!\n");
files.append("test.txt", "这是追加的内容\n");
files.append("test.txt", "时间: " + new Date().toLocaleString() + "\n");

let content = files.read("test.txt");
log("文件内容:\n" + content);

log("文件大小: " + files.size("test.txt") + " bytes");
log("文件是否存在: " + files.exists("test.txt"));

// 列出脚本目录
let list = files.list(".");
log("目录文件: " + list.join(", "));
