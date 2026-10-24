// AI生成
// 存储示例 —— 演示 storages 键值持久化
let storage = storages.create("my_app");

storage.put("username", "autojs_user");
storage.put("count", 0);
storage.put("settings", JSON.stringify({theme: "dark", fontSize: 14}));

log("用户名: " + storage.get("username"));
log("计数: " + storage.get("count"));
log("设置: " + storage.get("settings"));

storage.put("count", storage.get("count") + 1);
log("更新后计数: " + storage.get("count"));

log("包含 username: " + storage.contains("username"));
storage.remove("count");
log("删除后包含 count: " + storage.contains("count"));
