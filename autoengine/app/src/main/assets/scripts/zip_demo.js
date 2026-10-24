// AI生成
// 压缩解压示例 —— 演示 files.zip / files.unzip
files.write("a.txt", "文件A内容");
files.write("b.txt", "文件B内容");

files.zip(".", "backup.zip");
log("已压缩到 backup.zip");
log("压缩包大小: " + files.size("backup.zip") + " bytes");

files.mkdir("extracted");
files.unzip("backup.zip", "extracted");
log("已解压到 extracted/");

let list = files.list("extracted");
log("解压文件: " + list.join(", "));
