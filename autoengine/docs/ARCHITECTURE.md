# AutoEngine 架构文档

> 分层模块化架构 · 依赖方向自上而下 · 可扩展可维护

## 1. 分层总览

```
app/          应用层 — UI、导航、DI、生命周期
feature/      功能层 — 业务级功能（调度、打包）
toolkit/      能力层 — Android 系统能力封装（无障碍、视觉、悬浮窗…）
core/         核心层 — 引擎与 API 契约（无 UI、无 Android 框架依赖）
```

依赖规则：**上层可依赖下层，下层不可依赖上层**。

| 层 | Gradle 模块 | 可依赖 | 禁止依赖 |
|---|---|---|---|
| core | `:core:api`, `:core:runtime` | 外部 SDK | toolkit, feature, app |
| toolkit | `:toolkit:*` | core | feature, app |
| feature | `:feature:*` | core, toolkit | app |
| app | `:app` | 所有层 | — |

## 2. 模块清单

### core/ — 核心层

| 模块 | 路径 | 包名 | 职责 |
|------|------|------|------|
| api | `:core:api` | `org.autojs.core.api` | API 注解、注册器、脚本异常、脚本源抽象 |
| runtime | `:core:runtime` | `org.autojs.core.runtime` | Rhino 引擎、线程模型、事件循环、模块加载、沙箱 |

**依赖关系**：`runtime` → `api`（`api(project(Modules.CORE_API))`）

### toolkit/ — 能力层

| 模块 | 路径 | 包名 | 职责 |
|------|------|------|------|
| accessibility | `:toolkit:accessibility` | `org.autojs.toolkit.accessibility` | 无障碍服务、节点快照、UiSelector、手势 |
| vision | `:toolkit:vision` | `org.autojs.toolkit.vision` | MediaProjection 截图、模板匹配、找色、OCR |
| floaty | `:toolkit:floaty` | `org.autojs.toolkit.floaty` | 悬浮窗管理、XML→View 渲染、控制台 |
| network | `:toolkit:network` | `org.autojs.toolkit.network` | HTTP/WebSocket（OkHttp 封装） |
| storage | `:toolkit:storage` | `org.autojs.toolkit.storage` | 文件读写、MMKV 存储、压缩/解压 |
| device | `:toolkit:device` | `org.autojs.toolkit.device` | 设备信息、应用管理、对话框 |
| shell | `:toolkit:shell` | `org.autojs.toolkit.shell` | Shell 命令执行、Root 通道 |

**依赖关系**：所有 toolkit 模块 → `core:api`（`api(project(Modules.CORE_API))`）

### feature/ — 功能层

| 模块 | 路径 | 包名 | 职责 |
|------|------|------|------|
| scheduler | `:feature:scheduler` | `org.autojs.feature.scheduler` | 定时任务、触发器（解锁/充电/通知/App前台）、开机自启 |
| packager | `:feature:packager` | `org.autojs.feature.packager` | APK 打包流水线、脚本加密（AES-256-GCM）、签名 |

**依赖关系**：feature 模块 → `core:runtime`（`api(project(Modules.CORE_RUNTIME))`）

### app/ — 应用层

| 路径 | 包名 | 职责 |
|------|------|------|
| `:app` | `org.autojs.app` | 主界面、脚本列表、代码编辑器、日志面板、设置、打包向导 |

**依赖关系**：`app` → 所有其他模块（`implementation`）

## 3. 依赖图

```
                    ┌─────┐
                    │ app │
                    └──┬──┘
           ┌───────────┼───────────┐
           ▼           ▼           ▼
    ┌────────────┐ ┌────────┐ ┌─────────┐
    │  toolkit   │ │ feature │ │  core   │
    │ (7 modules)│ │ (2 mod) │ │ (2 mod) │
    └─────┬──────┘ └────┬───┘ └────┬────┘
          │               │          │
          └───────┬───────┘          │
                  ▼                  │
           ┌────────────┐            │
           │ core:api   │◄───────────┘
           └────────────┘
                  ▲
           ┌──────┴──────┐
           │ core:runtime│
           └─────────────┘
```

## 4. 模块内结构约定

每个模块遵循统一结构：

```
<module>/
├── build.gradle.kts
└── src/main/kotlin/org/autojs/<layer>/<module>/
    └── *.kt
```

- `build.gradle.kts` 中 `namespace` 与包名一致
- 模块间通过 `api()` 暴露契约，通过 `implementation()` 引入实现
- 外部依赖统一在 `buildSrc/src/main/kotlin/Deps.kt` 中声明版本

## 5. 扩展指南

### 新增 toolkit 模块

1. 创建 `toolkit/<name>/build.gradle.kts`（参考现有 toolkit 模块）
2. 创建源码目录 `toolkit/<name>/src/main/kotlin/org/autojs/toolkit/<name>/`
3. 在 `settings.gradle.kts` 添加 `include(":toolkit:<name>")`
4. 在 `Deps.kt` 的 `Modules` 对象添加路径常量
5. 在 `app/build.gradle.kts` 添加 `implementation(project(Modules.TOOLKIT_<NAME>))`

### 新增 feature 模块

同上，但目录为 `feature/<name>/`，包名为 `org.autojs.feature.<name>`，模块路径为 `:feature:<name>`。

### EASY_DESIGN.md 规划的新模块

按本架构，6 个新模块应归入以下层：

| 模块 | 归属层 | 路径 |
|------|--------|------|
| 积木编辑器 | feature | `feature/blocks/` |
| 操作录制器 | feature | `feature/recorder/` |
| 元素拾取器 | feature | `feature/inspector/` |
| AI 助手 | feature | `feature/ai/` |
| 模板引擎 | feature | `feature/templates/` |
| 可视化调试器 | feature | `feature/debug/` |

## 6. 重构记录

从平铺 12 模块重构为 4 层分组架构：

| 旧路径 | 新路径 | 旧包名 | 新包名 |
|--------|--------|--------|--------|
| `core-api/` | `core/api/` | `org.autojs.core.api` | 不变 |
| `core-runtime/` | `core/runtime/` | `org.autojs.core.runtime` | 不变 |
| `lib-accessibility/` | `toolkit/accessibility/` | `org.autojs.lib.accessibility` | `org.autojs.toolkit.accessibility` |
| `lib-vision/` | `toolkit/vision/` | `org.autojs.lib.vision` | `org.autojs.toolkit.vision` |
| `lib-floaty/` | `toolkit/floaty/` | `org.autojs.lib.floaty` | `org.autojs.toolkit.floaty` |
| `lib-net/` | `toolkit/network/` | `org.autojs.lib.net` | `org.autojs.toolkit.network` |
| `lib-io/` | `toolkit/storage/` | `org.autojs.lib.io` | `org.autojs.toolkit.storage` |
| `lib-device/` | `toolkit/device/` | `org.autojs.lib.device` | `org.autojs.toolkit.device` |
| `lib-shell/` | `toolkit/shell/` | `org.autojs.lib.shell` | `org.autojs.toolkit.shell` |
| `scheduler/` | `feature/scheduler/` | `org.autojs.scheduler` | `org.autojs.feature.scheduler` |
| `packager/` | `feature/packager/` | `org.autojs.packager` | `org.autojs.feature.packager` |
