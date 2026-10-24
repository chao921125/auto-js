# AutoJs Pro — API 文档

> 版本 v1.0 | 基于自研引擎，对标 AutoJS Pro

## 命名空间速查

| 命名空间 | 说明 | 优先级 |
|---------|------|--------|
| `auto` | 无障碍 UI 自动化 | P0 |
| `images` | 截图/找图/找色 | P0 |
| `ocr` | 文字识别 | P1 |
| `floaty` | 悬浮窗 | P0 |
| `ui` | 脚本 UI (ui.layout) | P1 |
| `files` | 文件读写/压缩 | P0 |
| `storages` | 键值持久化 | P0 |
| `http` | HTTP 请求 | P0 |
| `app` | 应用管理 | P0 |
| `device` | 设备信息 | P0 |
| `shell` | Shell 命令 | P1 |
| `threads` | 多线程 | P0 |
| `timers` | 定时器 | P0 |
| `events` | 事件总线 | P0 |
| `console` | 控制台 | P0 |
| `engines` | 引擎管理 | P0 |

---

## auto — 无障碍自动化

### 选择器
```javascript
auto.text("登录")          // 文本匹配
auto.textContains("登")    // 文本包含
auto.id("btn_submit")      // ID 匹配
auto.desc("按钮")          // 描述匹配
auto.clazz("Button")       // 类名匹配
auto.clickable()           // 可点击
auto.depth(3)              // 深度
```

### 查询方法
```javascript
selector.findOne(timeout)   // 查找单个（带超时 ms）
selector.findAll()          // 查找全部
selector.waitFor(timeout)   // 等待出现
selector.exists()           // 是否存在
selector.each(fn)           // 遍历
```

### 操作
```javascript
auto.click(x, y)            // 坐标点击
auto.longClick(x, y)        // 坐标长按
auto.swipe(x1,y1,x2,y2,dur) // 滑动
auto.gesture(dur, [x,y]...) // 手势路径
auto.input(text)            // 输入文本
auto.scrollDown()           // 向下滚动
auto.scrollUp()             // 向上滚动
auto.waitFor()              // 等待服务开启
```

---

## images — 视觉引擎

```javascript
images.requestScreenCapture()           // 申请截图权限
images.captureScreen()                  // 截图
images.findImage(src, template, opts)   // 找图
images.findColor(img, color, opts)      // 找色
images.findMultiColors(img, color, offsets) // 多点找色
images.detectsColor(img, x, y, color)   // 检测颜色
images.crop(img, [x,y,w,h])             // 裁剪
images.resize(img, w, h)                // 缩放
images.pixel(img, x, y)                 // 像素颜色
images.read(path)                       // 读取图片
images.save(img, path)                  // 保存图片
```

---

## ocr — 文字识别

```javascript
let result = ocr.recognize(img, {region: [x,y,w,h]})
// result.items: [{text, bounds, confidence}]
// result.fullText: 拼接文本
```

---

## floaty — 悬浮窗

```javascript
let w = floaty.window('<frame><text text="Hello"/></frame>')
floaty.close(w)
floaty.closeAll()
floaty.isOverlaysEnabled()
let console = floaty.console()
```

---

## http — 网络请求

```javascript
http.get(url, headers)        // GET 请求
http.post(url, data, headers) // POST 请求
http.postJson(url, json)      // POST JSON
http.download(url, dest)      // 下载文件
http.uploadFile(url, path)    // 上传文件
```

---

## files — 文件操作

```javascript
files.read(path)      files.write(path, content)
files.append(path, content)   files.copy(src, dest)
files.move(src, dest)         files.remove(path)
files.exists(path)   files.list(path)
files.zip(src, dest)  files.unzip(src, dest)
```

---

## storages — 键值存储

```javascript
let s = storages.create("namespace")
s.put("key", value)
s.get("key", default)
s.contains("key")
s.remove("key")
s.clear()
```

---

## device — 设备信息

```javascript
device.width       device.height
device.density     device.brand
device.model       device.sdkInt
device.battery     device.isCharging
device.androidId
```

---

## app — 应用管理

```javascript
app.launch(packageName)
app.isInstalled(packageName)
app.getAppName(packageName)
app.uninstall(packageName)
app.openUrl(url)
```

---

## shell — Shell 命令

```javascript
let r = shell.exec("ls -la", false)
// r.code, r.result, r.error
shell.isRootAvailable()
```

---

## threads — 多线程

```javascript
let t = threads.start(function() { /* ... */ })
threads.dispose(t)
threads.currentThread()
```

---

## events — 事件总线

```javascript
events.on("event", handler)
events.once("event", handler)
events.emit("event", data)
events.off("event", handler)
```

---

## 全局函数

```javascript
log(msg)           // 打印日志
sleep(ms)          // 休眠
require("module")  // 加载模块
setTimeout(fn, ms) // 定时器
setInterval(fn, ms)// 循环定时器
clearTimeout(id)   // 清除定时器
```
