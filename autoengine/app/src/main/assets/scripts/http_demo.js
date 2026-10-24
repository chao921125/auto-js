// AI生成
// HTTP 请求示例 —— 演示网络请求
log("发送 GET 请求...");

let resp = http.get("https://httpbin.org/get");
if (resp) {
    log("状态码: " + resp.statusCode);
    log("响应: " + resp.body.substring(0, 200));
}

log("发送 POST 请求...");
let postResp = http.postJson("https://httpbin.org/post", {
    name: "AutoJs",
    version: "1.0"
});
if (postResp) {
    log("POST 状态码: " + postResp.statusCode);
}
