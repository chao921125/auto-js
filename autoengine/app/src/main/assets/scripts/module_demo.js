// AI生成
// 模块加载示例 —— 演示 require
// utils.js 需在同一目录
let utils = require("./utils.js");

log("加载模块: " + utils.version);
let result = utils.add(10, 20);
log("10 + 20 = " + result);

let formatted = utils.formatDate(new Date());
log("当前时间: " + formatted);
