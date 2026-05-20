/**
 * Auto.js Pro 新特性使用示例
 * 
 * 本脚本演示了所有新增模块的实际应用场景
 * 包括: WebSocket通信、数据库存储、HTTP请求、定时任务等
 */

// ==================== 导入模块 ====================
var Database = org.cc.stardust.autojs.database.AutoJsDatabase;
var HttpClient = org.cc.stardust.autojs.http.EnhancedHttpClient;
var WebSocket = org.cc.stardust.autojs.websocket.AutoJsWebSocket;
var Scheduler = org.cc.stardust.autojs.scheduler.TaskScheduler;
var SystemSettings = org.cc.stardust.autojs.settings.SystemSettings;
var NodeEngineManager = org.cc.stardust.autojs.nodejs.NodeEngineManager;

// ==================== 全局变量 ====================
var db, client, ws, settings;
var userData = {};

// ==================== 初始化函数 ====================
function init() {
    console.log("🚀 开始初始化...");
    
    // 1. 初始化数据库
    db = new Database(context, "demo_db", 1);
    initDatabase();
    console.log("✅ 数据库初始化完成");
    
    // 2. 初始化HTTP客户端
    client = new HttpClient();
    console.log("✅ HTTP客户端初始化完成");
    
    // 3. 初始系统设置访问
    settings = new SystemSettings(activity);
    console.log("✅ 系统设置初始化完成");
    
    // 4. 保存初始数据
    saveInitialData();
}

// ==================== 数据库初始化 ====================
function initDatabase() {
    // 创建用户表
    db.createTable("users", 
        "CREATE TABLE IF NOT EXISTS users (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "name TEXT NOT NULL, " +
        "email TEXT, " +
        "score INTEGER DEFAULT 0, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
    );
    
    // 创建活动记录表
    db.createTable("activity_logs",
        "CREATE TABLE IF NOT EXISTS activity_logs (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "action TEXT, " +
        "details TEXT, " +
        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
    );
    
    console.log("📊 数据表创建完成");
}

// ==================== 保存初始数据 ====================
function saveInitialData() {
    var values = new db.ContentValues();
    values.put("name", "张三");
    values.put("email", "zhangsan@example.com");
    values.put("score", 100);
    
    var userId = db.insert("users", null, values);
    console.log("用户ID:", userId);
    
    // 记录操作日志
    var logValues = new db.ContentValues();
    logValues.put("action", "INIT_APP");
    logValues.put("details", "应用首次初始化");
    db.insert("activity_logs", null, logValues);
}

// ==================== HTTP请求示例 ====================
function fetchUserData() {
    try {
        console.log("🌐 开始获取用户数据...");
        
        // GET请求
        var response = client.get("https://jsonplaceholder.typicode.com/users/1");
        
        if (response.isSuccess()) {
            var user = JSON.parse(response.body);
            console.log("用户名称:", user.name);
            console.log("邮箱:", user.email);
            
            // 更新本地数据库
            updateLocalUser(user);
        } else {
            console.error("❗ 获取用户数据失败:", response.statusCode);
        }
        
    } catch (e) {
        console.error("❗ 请求异常:", e);
    }
}

// ==================== 更新本地用户数据 ====================
function updateLocalUser(remoteUser) {
    try {
        var updateValues = new db.ContentValues();
        updateValues.put("name", remoteUser.name);
        updateValues.put("email", remoteUser.email);
        updateValues.put("score", 150);
        
        var rowsUpdated = db.update("users", updateValues, "id = ?", ["1"]);
        console.log("更新了", rowsUpdated, "条记录");
        
        // 记录活动日志
        var logValues = new db.ContentValues();
        logValues.put("action", "SYNC_DATA");
        logValues.put("details", "同步远程数据: " + remoteUser.name);
        db.insert("activity_logs", null, logValues);
        
    } catch (e) {
        console.error("❗ 更新数据失败:", e);
    }
}

// ==================== WebSocket通信示例 ====================
function setupWebSocket() {
    try {
        console.log("🔌 连接WebSocket服务器...");
        
        // 创建WebSocket连接
        ws = new WebSocket("wss://echo.websocket.org");
        
        // 监听连接打开
        ws.on("open", function(data) {
            console.log("✅ WebSocket连接已建立");
            
            // 发送测试消息
            var message = JSON.stringify({
                type: "hello",
                timestamp: new Date().toISOString(),
                appVersion: "1.0.0"
            });
            
            ws.send(message);
            console.log("📤 发送消息:", message);
        });
        
        // 监听接收到的消息
        ws.on("message", function(data) {
            console.log("📥 收到消息:", data.data);
            
            try {
                var response = JSON.parse(data.data);
                
                // 处理不同类型的消息
                if (response.type === "notification") {
                    showNotification(response.content);
                } else if (response.type === "command") {
                    executeCommand(response);
                }
                
            } catch (e) {
                console.log("非JSON消息:", data.data);
            }
        });
        
        // 监听连接关闭
        ws.on("close", function(data) {
            console.log("🔒 WebSocket连接已关闭:", data.reason);
        });
        
        // 监听错误
        ws.on("error", function(data) {
            console.error("❗ WebSocket错误:", data.message);
        });
        
    } catch (e) {
        console.error("❗ WebSocket初始化失败:", e);
    }
}

// ==================== 显示通知 ====================
function showNotification(content) {
    console.log("📢 收到通知:", content);
    
    // 使用Android原声通知
    try {
        var NotificationCompat = android.app.Notification$Builder;
        var NotificationManager = context.getSystemService(context.NOTIFICATION_SERVICE);
        
        var builder = new NotificationCompat(context, "default_channel")
            .setContentTitle("Auto.js通知")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true);
        
        NotificationManager.notify(1001, builder.build());
        
    } catch (e) {
        console.error("❗ 显示通知失败:", e);
    }
}

// ==================== 执行命令 ====================
function executeCommand(command) {
    console.log("⚙️ 执行命令:", command.type);
    
    switch (command.type) {
        case "fetch_data":
            fetchUserData();
            break;
            
        case "get_stats":
            sendStatistics();
            break;
            
        case "shutdown":
            console.log("🛑 收到关机命令");
            if (ws) ws.close();
            break;
    }
}

// ==================== 发送统计数据 ====================
function sendStatistics() {
    try {
        // 查询数据库统计
        var userCount = db.getCount("users");
        var logCount = db.getCount("activity_logs");
        
        var stats = {
            type: "statistics",
            users: userCount,
            logs: logCount,
            timestamp: new Date().toISOString()
        };
        
        if (ws && ws.isConnected()) {
            ws.send(JSON.stringify(stats));
            console.log("📤 已发送统计数据:", JSON.stringify(stats));
        }
        
    } catch (e) {
        console.error("❗ 发送统计失败:", e);
    }
}

// ==================== 系统设置操作 ====================
function manageSystemSettings() {
    try {
        console.log("⚙️ 管理系统设置...");
        
        // 获取当前屏幕超时
        var currentTimeout = settings.getSystem("screen_off_timeout");
        console.log("当前屏幕超时:", currentTimeout, "毫秒");
        
        // 获取屏幕亮度
        var brightness = settings.getInt("system", "screen_brightness", 128);
        console.log("当前屏幕亮度:", brightness);
        
        // 使用快捷访问
        var quick = settings.quick();
        
        // 检查自动亮度
        if (quick.isAutoBrightness()) {
            console.log("✓ 自动亮度已开启");
        }
        
        // 临时增加屏幕亮度(便于截图)
        quick.setScreenBrightness(200);
        console.log("已提升屏幕亮度到200");
        
        // 恢复原始亮度
        setTimeout(function() {
            quick.setScreenBrightness(brightness);
            console.log("已恢复原始亮度");
        }, 5000);
        
    } catch (e) {
        console.error("❗ 设置操作失败:", e);
    }
}

// ==================== 定时任务示例 ====================
function setupScheduledTasks() {
    try {
        console.log("⏰ 设置定时任务...");
        
        var scheduler = Scheduler.getInstance(activity);
        
        // 任务1: 每5分钟记录一次活动
        scheduler.addIntervalTask(
            "activity_monitor",
            300000, // 5分钟
            currentFrame.context.filePath
        );
        console.log("✓ 已添加活动监控任务(每5分钟)");
        
        // 任务2: 每天早晨8点生成日报
        scheduler.addCronTask(
            "daily_report",
            "0 8 * * *", // Cron表达式: 分 时 日 月 周
            "/path/to/daily_report.js"
        );
        console.log("✓ 已添加每日报告任务(每天8:00)");
        
        // 任务3: 10分钟后执行一次性备份
        scheduler.addOneTimeTask(
            "data_backup",
            600000, // 10分钟
            "/path/to/backup.js"
        );
        console.log("✓ 已添加备份任务(10分钟后)");
        
        // 查看所有任务
        var tasks = scheduler.getAllTasks();
        console.log("当前活跃任务数:", tasks.length);
        
    } catch (e) {
        console.error("❗ 定时任务设置失败:", e);
    }
}

// ==================== 数据同步示例 ====================
function syncData() {
    try {
        console.log("🔄 开始数据同步...");
        
        // 从远程获取最新数据
        var response = client.get("https://jsonplaceholder.typicode.com/posts?userId=1");
        
        if (response.isSuccess()) {
            var posts = JSON.parse(response.body);
            console.log("获取到", posts.length, "条帖子");
            
            // 保存到本地数据库
            db.beginTransaction();
            try {
                for (var i = 0; i < posts.length; i++) {
                    var post = posts[i];
                    
                    var values = new db.ContentValues();
                    values.put("title", post.title);
                    values.put("body", post.body);
                    values.put("userId", post.userId);
                    
                    db.insert("posts", null, values);
                }
                
                db.commit();
                console.log("✅ 成功同步", posts.length, "条数据");
                
            } catch (e) {
                db.rollback();
                console.error("❗ 同步失败已回滚:", e);
            }
            
        } else {
            console.error("❗ 获取远程数据失败:", response.statusCode);
        }
        
    } catch (e) {
        console.error("❗ 同步异常:", e);
    }
}

// ==================== 异常处理与日志 ====================
function logActivity(action, details) {
    try {
        var values = new db.ContentValues();
        values.put("action", action);
        values.put("details", details || "");
        
        db.insert("activity_logs", null, values);
        
    } catch (e) {
        console.error("❗ 日志记录失败:", e);
    }
}

// ==================== 清理资源 ====================
function cleanup() {
    console.log("🧹 清理资源...");
    
    if (ws) {
        ws.close();
        console.log("✓ WebSocket已关闭");
    }
    
    if (db) {
        db.close();
        console.log("✓ 数据库已关闭");
    }
    
    logActivity("APP_CLOSE", "应用正常关闭");
}

// ==================== 主函数 ====================
function main() {
    try {
        // 1. 初始化
        init();
        
        // 2. 获取远程数据
        fetchUserData();
        
        // 3. 设置WebSocket
        setupWebSocket();
        
        // 4. 管理系统设置
        manageSystemSettings();
        
        // 5. 设置定时任务
        setupScheduledTasks();
        
        // 6. 定期数据同步
        setInterval(function() {
            syncData();
        }, 60000); // 每分钟同步一次
        
        console.log("🎉 所有任务启动完成!");
        console.log("💡 按音量上键停止脚本");
        
        // 监听退出
        events.observeKey();
        events.onceKeyDown("volume_up", function() {
            console.log("🛑 收到停止信号");
            cleanup();
            exit();
        });
        
    } catch (e) {
        console.error("❗ 主流程异常:", e);
        logActivity("ERROR", e.message || e.toString());
    }
}

// 启动应用
console.log("=".repeat(50));
console.log("  Auto.js Pro 新特性演示");
console.log("  版本: 1.0.0");
console.log("=".repeat(50));

main();
