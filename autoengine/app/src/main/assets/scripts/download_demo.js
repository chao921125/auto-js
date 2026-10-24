// AI生成
// 文件下载示例 —— 演示 http.download
log("开始下载文件...");

let success = http.download(
    "https://httpbin.org/stream/5",
    files.exists("downloads") || files.mkdir("downloads"),
    {}
);

if (success) {
    log("下载成功");
} else {
    log("下载失败");
}

// 上传文件示例
if (files.exists("test.txt")) {
    let resp = http.uploadFile("https://httpbin.org/post", "test.txt", "file");
    log("上传状态码: " + resp.statusCode);
}
