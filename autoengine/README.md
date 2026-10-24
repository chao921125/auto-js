# AutoEngine — 类 AutoJS Pro 自动化引擎

> Android 通用自动化引擎 + 脚本开发 IDE | 零 Root | JavaScript 脚本

## 项目简介

在 Android 手机上运行的通用自动化引擎 + 脚本开发 IDE：

- 以 **JavaScript** 为脚本语言，提供 `auto`（无障碍 UI 自动化）、`images`（找图/找色/OCR）、`floaty`（悬浮窗）、`shell`、`http`、`files`、`app`、`device`、`threads` 等成体系的脚本 API
- **零 Root** 即可驱动任意第三方 App 与系统界面（基于无障碍服务 + 屏幕采集 + 手势注入）
- 手机端完成 **「编写 → 调试 → 运行 → 打包分发」** 的完整闭环

## 技术栈

| 项 | 选型 |
|---|---|
| 语言 | Kotlin + Java |
| minSdk / targetSdk | API 24 (Android 7.0) / 35 |
| JS 引擎 | Rhino 1.7.15 (解释模式, optimizationLevel=-1) |
| 网络 | OkHttp 4.x |
| 图像 | OpenCV 4.x + 自研颜色扫描 |
| OCR | ML Kit Text Recognition v2 (中文) |
| 存储 | MMKV + Room |
| UI | Material Components 3 |
| 构建 | Gradle 8.10 + AGP 8.6 |

## 工程结构

采用分层架构，按职责划分模块，依赖方向严格自上而下：

```
autoengine/
├── core/                          # 核心层 — 引擎与 API 契约（无 UI 依赖）
│   ├── api/                       # 脚本 API 命名空间注册层
│   └── runtime/                   # ScriptRuntime：Rhino 引擎/线程模型/事件循环/沙箱
├── toolkit/                       # 能力层 — Android 系统能力封装
│   ├── accessibility/             # 无障碍引擎（服务、节点快照、选择器、手势）
│   ├── vision/                    # 截图通道、找图、找色、OCR
│   ├── floaty/                    # 悬浮窗 + XML→View 渲染器
│   ├── network/                   # http / websocket
│   ├── storage/                   # files / storages(MMKV) / zip
│   ├── device/                    # app / device / dialogs / events
│   └── shell/                     # shell / root 通道
├── feature/                       # 功能层 — 业务级功能模块
│   ├── scheduler/                 # 触发器、定时、保活引导
│   └── packager/                  # 打包流水线 + 签名 + 模板 APK
├── app/                           # 应用层 — 主界面、编辑器、脚本管理
├── buildSrc/                      # 构建逻辑：依赖收敛 (Deps.kt)
└── docs/                          # 文档
```

### 分层依赖规则

| 层 | 可依赖 | 不可依赖 |
|---|---|---|
| `core` | 仅外部 SDK | toolkit / feature / app |
| `toolkit` | core | feature / app |
| `feature` | core / toolkit | app |
| `app` | 所有层 | — |

## 核心模块

### core/runtime — 脚本引擎
- `ScriptRuntime`: 封装 Rhino 引擎 + 线程模型 + 事件循环 + 沙箱
- `ScriptThread`: 每线程独立 Rhino Context，自建事件循环
- `EventLoop`: setTimeout/setInterval + microtask 队列
- `ModuleLoader`: require() 三级解析（文件 → 内置 → 插件）
- `SandboxShutter`: ClassShutter 安全隔离

### toolkit/accessibility — 无障碍自动化
- `AutoJsAccessibilityService`: 核心服务入口
- `AccessibilityEngine`: 窗口快照、去抖合并、查询与操作
- `UiSelector`: 链式条件选择器
- 操作兜底链: ACTION_CLICK → 父链查找 → dispatchGesture

### toolkit/vision — 视觉引擎
- `ScreenCapturer`: MediaProjection 截图通道
- `ImageMatcher`: 模板匹配 (NCC) + 多尺度
- `ColorFinder`: 找色/多点找色/颜色检测
- `OcrEngine`: ML Kit 中文 OCR

### feature/packager — APK 打包器
- `ApkPackager`: 收集→加密→组装→签名→输出
- `ScriptEncryptor`: AES-256-GCM 脚本加密
- `ApkSigner`: V1+V2 签名

## 快速开始

```bash
# 1. 克隆项目
cd autoengine

# 2. 使用 Gradle Wrapper 构建
./gradlew assembleDebug

# 3. 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 首次使用
1. 打开 App，在设置中开启**无障碍服务**
2. 如需悬浮窗，开启**悬浮窗权限**
3. 如需截图/找图，首次使用 `images.requestScreenCapture()` 时授权
4. 在脚本列表中新建或导入脚本，点击运行

## 示例脚本

项目内置 30+ 示例脚本（`app/src/main/assets/scripts/`）：

| 脚本 | 说明 |
|------|------|
| `wechat_signin.js` | 微信签到（无障碍+找图） |
| `auto_click.js` | 自动点击控件 |
| `swipe_demo.js` | 滑动操作 |
| `find_color.js` | 屏幕找色 |
| `http_demo.js` | HTTP 请求 |
| `file_ops.js` | 文件读写 |
| `threads_demo.js` | 多线程 |
| `timer_demo.js` | 定时器 |
| `floaty_demo.js` | 悬浮窗 |
| `device_info.js` | 设备信息 |
| `ocr_demo.js` | OCR 文字识别 |
| `ui_demo.js` | 脚本 UI (ui.layout) |
| ... | ... |

## API 文档

详见 [docs/API.md](docs/API.md)

## 架构设计

```
┌───────────────────────────────────────────────────────────────┐
│ app          主界面 / 脚本管理 / 编辑器IDE / 日志面板          │
├───────────────────────────────────────────────────────────────┤
│ feature      scheduler 定时触发 · packager 打包流水线           │
├───────────────────────────────────────────────────────────────┤
│ toolkit      accessibility · vision · floaty · network         │
│              storage · device · shell                          │
├───────────────────────────────────────────────────────────────┤
│ core         api API 契约 · runtime Rhino引擎/线程/事件循环/沙箱│
├───────────────────────────────────────────────────────────────┤
│ SDK          Android SDK · Rhino · OkHttp · ML Kit · MMKV      │
└───────────────────────────────────────────────────────────────┘
```

详见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

## 里程碑

- **M1 MVP**: Rhino 引擎 + auto/files/http/console + 编辑器 + 运行/日志/停止 ✅
- **M2**: 视觉引擎 + 悬浮窗 + ui.layout + threads/timers/events + 触发器 ✅
- **M3**: 打包器 v1 + License + OCR + 保活引导 ✅
- **M4**: 打包器 v2 (aapt2) + 插件系统 + QuickJS (可选)

## 许可

自研引擎，仅参考公开 API 形态设计，不借鉴 GPL 系开源分支代码。
