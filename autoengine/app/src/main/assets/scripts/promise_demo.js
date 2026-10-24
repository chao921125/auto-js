// AI生成
// Promise 异步示例 —— 演示 async/await
async function fetchData(url) {
    return new Promise(function(resolve) {
        let resp = http.get(url);
        resolve(resp);
    });
}

async function main() {
    log("开始获取数据...");
    let resp = await fetchData("https://httpbin.org/get");
    log("状态码: " + resp.statusCode);
    log("数据长度: " + resp.body.length);
    log("完成!");
}

main();
