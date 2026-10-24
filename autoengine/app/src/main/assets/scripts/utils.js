// AI生成
// utils.js —— 模块工具函数
module.exports = {
    version: "1.0.0",
    add: function(a, b) { return a + b; },
    sub: function(a, b) { return a - b; },
    mul: function(a, b) { return a * b; },
    div: function(a, b) { return b !== 0 ? a / b : NaN; },
    formatDate: function(d) {
        return d.getFullYear() + "-" +
            String(d.getMonth()+1).padStart(2,'0') + "-" +
            String(d.getDate()).padStart(2,'0') + " " +
            String(d.getHours()).padStart(2,'0') + ":" +
            String(d.getMinutes()).padStart(2,'0');
    },
    randomInt: function(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }
};
