# Auto.js Pro 新特性API文档

## 📚 目录

1. [概述](#概述)
2. [Node.js引擎支持](#nodejs引擎支持)
3. [WebSocket模块](#websocket模块)
4. [数据库模块](#数据库模块)
5. [增强HTTP模块](#增强http模块)
6. [任务调度模块](#任务调度模块)
7. [系统设置模块](#系统设置模块)
8. [完整示例](#完整示例)

---

## 概述

本文档介绍Auto.js修改版新增的Pro特性功能模块。这些模块基于Auto.js Pro的新特性整合而来,提供了更强大的自动化能力。

### 核心特性

- ✅ **Node.js引擎** - V8引擎支持,ES2021特性
- ✅ **WebSocket** - 双向实时通信
- ✅ **数据库** - SQLite本地数据存储
- ✅ **增强HTTP** - 完整的HTTP客户端
- ✅ **任务调度** - Cron风格定时任务
- ✅ **系统设置** - 系统参数读写

---

## Node.js引擎支持

### 功能说明

Auto.js Pro引入了双引擎架构:
- **Rhino引擎**(第一代API) - 默认引擎,兼容性好的JavaScript引擎
- **Node.js引擎**(第二代API) - 基于V8,性能提升100倍,支持ES2021

### API使用

```javascript
// 获取引擎管理器
var nodeEngine = org.cc.stardust.autojs.nodejs.NodeEngineManager;
var manager = nodeEngine.getInstance(activity);

// 切换到Node.js引擎
manager.switchToNodeEngine();

// 执行脚本
manager.executeScript("/path/to/script.js");

// 评估表达式
var result = manager.eval("1 + 2 * 3");

// 切换回Rhino引擎
manager.switchToRhinoEngine();

// 检查引擎是否可用
if (manager.isNodeEngineAvailable()) {
    console.log("Node.js引擎已启用");
}
```

### 代码加密

```javascript
// 加密脚本内容
var password = "my_password";
var encrypted = nodeEngine.EncryptionUtils.encryptScript(scriptContent, password);

// 解密脚本内容
var decrypted = nodeEngine.EncryptionUtils.decryptScript(encryptedData, password);
```

---

## WebSocket模块

### 功能说明

提供完整的WebSocket客户端支持,实现双向实时通信。

### API使用

```javascript
// 引入WebSocket类
var WebSocket = org.cc.stardust.autojs.websocket.AutoJsWebSocket;

// 创建连接
var ws = new WebSocket("ws://example.com/socket");

// 注册事件监听器
ws.on("open", function(data) {
    console.log("连接已建立:", data);
});

ws.on("message", function(data) {
    console.log("收到消息:", data.data);
});

ws.on("close", function(data) {
    console.log("连接已关闭:", data.reason);
});

ws.on("error", function(data) {
    console.error("发生错误:", data.message);
});

// 发送消息
ws.send(JSON.stringify({type: "hello", data: "world"}));

// 关闭连接
ws.close();

// 检查连接状态
if (ws.isConnected()) {
    console.log("连接正常");
}
```

### 高级配置

```javascript
// 自定义配置
var Config = org.cc.stardust.autojs.websocket.AutoJsWebSocket.WebSocketConfig;
var config = new Config()
    .setReconnectDelay(5000)      // 重连延迟5秒
    .setMaxReconnectAttempts(10)   // 最多重连10次
    .setAutoReconnect(true);       // 启用自动重连

var ws = new WebSocket("ws://example.com/socket", config);
```

---

## 数据库模块

### 功能说明

SQLite数据库封装,支持JSON格式数据读写和事务管理。

### API使用

```javascript
// 引入数据库类
var Database = org.cc.stardust.autojs.database.AutoJsDatabase;
var ContentValues = Database.ContentValues;

// 创建/打开数据库
var db = new Database(context, "mydb", 1);

// 创建表
db.createTable("users", 
    "CREATE TABLE IF NOT EXISTS users (" +
    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
    "name TEXT, " +
    "age INTEGER, " +
    "email TEXT)"
);

// 插入数据
var values = new ContentValues();
values.put("name", "张三");
values.put("age", 25);
values.put("email", "zhang@example.com");

db.insert("users", null, values);

// 查询数据
var results = db.queryToJsonArray("users", null, "age > 18");
console.log("结果数量:", results.length());

for (var i = 0; i < results.length(); i++) {
    var user = results.getJSONObject(i);
    console.log("用户:", user.getString("name"));
}

// 使用预处理语句(防SQL注入)
var stmt = db.prepareStatement("SELECT * FROM users WHERE age > ?");
var cursor = stmt.query(20);

// 更新数据
var updateValues = new ContentValues();
updateValues.put("name", "李四");
db.update("users", updateValues, "id = ?", ["1"]);

// 删除数据
db.delete("users", "id = ?", ["1"]);

// 事务处理
db.beginTransaction();
try {
    // 批量操作
    for (var i = 0; i < 100; i++) {
        var v = new ContentValues();
        v.put("name", "user" + i);
        db.insert("users", null, v);
    }
    db.commit(); // 提交事务
} catch (e) {
    db.rollback(); // 回滚事务
    console.error("事务失败:", e);
}

// 获取记录数
var count = db.getCount("users");
console.log("总记录数:", count);

// 关闭数据库
db.close();
```

---

## 增强HTTP模块

### 功能说明

完整的HTTP客户端,支持所有HTTP方法、文件上传下载、Cookie管理等。

### API使用

```javascript
// 引入HTTP客户端
var HttpClient = org.cc.stardust.autojs.http.EnhancedHttpClient;
var client = new HttpClient();

// GET请求
var response = client.get("https://api.example.com/users");
console.log("状态码:", response.statusCode);
console.log("响应体:", response.body);

// POST请求(JSON)
var postData = JSON.stringify({name: "张三", age: 25});
var postResponse = client.post("https://api.example.com/users", postData);

// PUT请求
var putData = JSON.stringify({id: 1, name: "李四"});
client.put("https://api.example.com/users/1", putData);

// DELETE请求
client.delete("https://api.example.com/users/1");

// 自定义请求头
var headers = {
    "Authorization": "Bearer token123",
    "Content-Type": "application/json"
};
var customResponse = client.get("https://api.example.com/data", headers);

// 文件下载
var DownloadCallback = org.cc.stardust.autojs.http.EnhancedHttpClient.DownloadProgressCallback;
var callback = new DownloadCallback() {
    onProgress: function(current, total) {
        var percent = (current * 100 / total).toFixed(2);
        console.log("下载进度:", percent + "%");
    }
};

var result = client.download(
    "https://example.com/file.zip",
    "/sdcard/Download/file.zip",
    callback
);

if (result.success) {
    console.log("下载完成:", result.filePath);
}

// 配置选项
var Config = org.cc.stardust.autojs.http.EnhancedHttpClient.HttpConfig;
var config = new Config()
    .setConnectTimeout(15000)    // 连接超时15秒
    .setReadTimeout(30000)       // 读取超时30秒
    .setIgnoreSsl(true)          // 忽略SSL验证
    .setMaxRetries(3);           // 最大重试3次

client.setConfig(config);
```

---

## 任务调度模块

### 功能说明

Cron风格的定时任务调度系统,支持系统级闹钟。

### API使用

```javascript
// 引入调度器
var Scheduler = org.ccstardust.autojs.scheduler.TaskScheduler;
var scheduler = Scheduler.getInstance(activity);

// 添加Cron风格定时任务
// Cron格式: 分 时 日 月 周
scheduler.addCronTask("daily_task", "0 8 * * *", "/path/to/check_in.js");
// 每天8:00执行签到脚本

scheduler.addCronTask("every_2_hours", "0 */2 * * *", "/path/to/task.js");
// 每2小时执行一次

scheduler.addCronTask("weekly_task", "0 9 * * 1", "/path/to/weekly.js");
// 每周一9:00执行

// 添加一次性任务
scheduler.addOneTimeTask("once_task", 60000, "/path/to/execute.js");
// 60秒后执行

// 添加间隔重复任务(单位:毫秒)
scheduler.addIntervalTask("interval_task", 5000, "/path/to/run.js");
// 每5秒执行一次

// 查看任务列表
var tasks = scheduler.getAllTasks();
for (var i = 0; i < tasks.length; i++) {
    console.log("任务ID:", tasks[i].id);
    console.log("下次执行:", tasks[i].nextExecutionTime);
}

// 删除任务
scheduler.removeTask("daily_task");

// 停止所有任务
scheduler.stopAllTasks();
```

### Cron表达式说明

格式: `分钟 小时 日 月 周`

| 表达式 | 含义 |
|--------|------|
| `0 8 * * *` | 每天8:00 |
| `0 */2 * * *` | 每2小时 |
| `30 9 * * 1` | 每周一9:30 |
| `0 0 1 * *` | 每月1号0:00 |
| `*/5 * * * *` | 每5分钟 |

---

## 系统设置模块

### 功能说明

安全的系统设置读写接口,支持Global/Secure/System三种范围。

### API使用

```javascript
// 引入设置类
var SysSettings = org.cc.stardust.autojs.settings.SystemSettings;
var settings = new SysSettings(activity);

// 读取设置
var airplaneMode = settings.getGlobal("airplane_mode_on");
var wifiEnabled = settings.getSecure("wifi_on");
var screenTimeout = settings.getSystem("screen_off_timeout");

// 写入设置(需要WRITE_SETTINGS权限)
settings.setSystem("screen_off_timeout", 60000); // 60秒超时
settings.setSecure("bluetooth_on", "1"); // 开启蓝牙

// 整数设置
var brightness = settings.getInt("system", "screen_brightness", 128);
settings.setInt("system", "screen_brightness", 200);

// 布尔设置
var autoBrightness = settings.getBoolean("system", "screen_brightness_mode", false);
settings.setBoolean("system", "screen_brightness_mode", true);

// 快捷访问
var quick = settings.quick();

// 获取屏幕超时
var timeout = quick.getScreenTimeout();

// 设置屏幕超时(30秒)
quick.setScreenTimeout(30000);

// 获取屏幕亮度
var brightness = quick.getScreenBrightness();

// 设置屏幕亮度(0-255)
quick.setScreenBrightness(150);

// 检查功能状态
if (quick.isAirplaneModeOn()) {
    console.log("飞行模式已开启");
}

if (quick.isWifiEnabled()) {
    console.log("WiFi已开启");
}

if (quick.isAutoBrightness()) {
    console.log("自动亮度已开启");
}
```

### 权限要求

```xml
<!-- AndroidManifest.xml中添加 -->
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## 完整示例

### 自动化签到脚本

```javascript
// 导入所有模块
var Database = org.cc.stardust.autojs.database.AutoJsDatabase;
var HttpClient = org.cc.stardust.autojs.http.EnhancedHttpClient;
var WebSocket = org.cc.stardust.autojs.websocket.AutoJsWebSocket;
var Scheduler = org.cc.stardust.autojs.scheduler.TaskScheduler;

// 初始化
var db = new Database(context, "autodaily", 1);
var client = new HttpClient();

// 创建数据库表
db.createTable("checkin_records", 
    "CREATE TABLE IF NOT EXISTS records (" +
    "id INTEGER PRIMARY KEY, " +
    "date TEXT, " +
    "status TEXT, " +
    "points INTEGER)"
);

// 签到函数
function checkIn() {
    try {
        // 获取用户信息
        var userResponse = client.get("https://api.example.com/user");
        var user = JSON.parse(userResponse.body);
        
        // 执行签到
        var checkinResponse = client.post(
            "https://api.example.com/checkin",
            JSON.stringify({userId: user.id})
        );
        
        var result = JSON.parse(checkinResponse.body);
        
        // 保存记录到数据库
        var values = new db.ContentValues();
        values.put("date", new Date().toISOString());
        values.put("status", result.success ? "success" : "failed");
        values.put("points", result.points || 0);
        
        db.insert("records", null, values);
        
        console.log("签到结果:", result);
        
    } catch (e) {
        console.error("签到失败:", e);
    }
}

// 设置为每天8点执行
Scheduler.getInstance(activity).addCronTask(
    "auto_checkin",
    "0 8 * * *",
    currentFrame.context.filePath
);

// 启动WebSocket监听
var ws = new WebSocket("ws://example.com/commands");
ws.on("message", function(data) {
    var command = JSON.parse(data.data);
    if (command.type === "stop_checkin") {
        ws.close();
        Scheduler.getInstance(activity).removeTask("auto_checkin");
    }
});
```

---

## 注意事项

1. **权限管理**: 部分功能需要特殊权限,请在AndroidManifest.xml中声明
2. **线程安全**: 大部分API在主线程调用,避免阻塞UI
3. **异常处理**: 建议对所有设置操作进行try-catch处理
4. **资源释放**: 使用完数据库后务必调用close()
5. **版本兼容**: 不同Android版本可能有不同的权限要求

---

## 更新日志

### v1.0.0 (2024)
- ✨ 初始版本发布
- ✅ Node.js引擎支持
- ✅ WebSocket模块
- ✅ 数据库模块
- ✅ 增强HTTP模块
- ✅ 任务调度模块
- ✅ 系统设置模块

---

## 技术支持

如有问题请参考:
- 官方文档: https://github.com/TonyJiangWJ/Auto.js
- Issue提交: https://github.com/TonyJiangWJ/Auto.js/issues
- QQ交流群: 1053378738
