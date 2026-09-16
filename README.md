# ExitGuard 🛡️

**专为严苛风控环境打造的 Android 安全应用启动台 / 出口网络哨兵。**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%2010%2B-green.svg)](https://developer.android.com)
[![Package Size](https://img.shields.io/badge/Package%20Size-2.4%20MB-success.svg)]()
[![Architecture](https://img.shields.io/badge/Arch-arm64--v8a%20%7C%20armeabi--v7a-orange.svg)]()

ExitGuard 允许您在受控环境下集中管理与启动手机中的关键应用。为每个应用独立设置出口 IP（支持 IPv4 / IPv6）或出口地区白名单。**启动目标应用前毫秒级校验当前公网出口，唯有完全匹配安全规则才放行启动，否则坚决阻断。**

---

## 🎯 诞生初衷与解决的痛点

在日常使用手机时，我们经常为了处理不同事务而在多个网络代理节点之间来回切换。然而这种高频切换存在显著痛点：

### 1. 节点跳变与风控封号（防“串节点”）
使用某些对 IP 变动与归属地极为敏感的高风控服务（如 Anthropic Claude 等各类顶级 AI 工具）时，一旦临时切了节点做别的事，事后**忘记切回固定节点就随手点开应用**，极易触发风控导致账号惨遭封禁。ExitGuard 在启动瞬间充当安全门神，杜绝“误触即被封”。

### 2. 专属浏览器 / Web 工作台隔离
许多用户会专门使用某款浏览器专职访问特定 AI 或特定服务的网页端。通过 ExitGuard，可以为该浏览器绑定专属的节点地区或出口 IP，确保每次打开时网络出口始终符合预期。

### 3. 告别翻找，专属聚合启动台
不局限于 AI 工具，ExitGuard 适用于所有对网络一致性有严格要求的应用。同时它也是一个简洁高效的私密工作台，受保护的应用集中陈列，无需在翻阅满屏桌面图标中到处寻找。

---

## ⚠️ 安全边界与原则声明（诚实公开）

- **只防护“启动瞬间”**：ExitGuard 的定位是**启动前守卫（Pre-launch Guard）**。它能 100% 拦截“忘记切节点而误启动”的情况。但若应用已经启动放行后，您在通知栏或后台手动切换了代理节点，ExitGuard 无法且不试图篡改运行时连接。
- **纯粹透明，无隐私泄露风险**：
  - 核心出口检测机制直接请求 **Cloudflare 官方公开追踪端点**：`https://www.cloudflare.com/cdn-cgi/trace`（备用 `https://1.1.1.1/cdn-cgi/trace`）。
  - **绝不经过任何第三方私人服务器或中转代理**，代码完全开源，绝不上传任何设备数据或访问日志。
- **极度克制，绿色无感**：
  - **无需 Root 权限**；
  - **无需 VpnService**（不占用系统的 VPN 槽位，可与各类代理客户端完美并存）；
  - **零后台常驻进程**（点开即检，放行即走，不偷跑电量与内存）。

---

## ✨ 核心特性

- 🔒 **严格 IP 模式（默认）**：
  - 支持 **IPv4** 与 **IPv6** 双栈地址，内置等价规范化解析（完美支持 IPv6 大小写与压缩/展开式等价判定）。
  - 支持一键快捷添加当前公网出口 IP，免去手动复制粘贴的繁琐操作。
  - 支持自定义配置多条备用 IP 白名单。

- 🌍 **出口地区模式**：
  - 支持配置允许的 ISO 国家/地区两位代码（如 `US`、`JP`、`SG`、`HK`、`GB` 等）。
  - 内置常见热门节点一键快速点选，自由组合。

- 🚫 **零容忍拦截策略**：
  - 出口 IP / 地区不符：**阻断启动**
  - 检测超时或无网络连接：**阻断启动**
  - 出口 API 返回异常数据：**阻断启动**
  - 应用规则未配置完毕：**阻断启动**
  - **绝不提供“无视警告继续启动”的绕过按钮**，杜绝任何因侥幸心理造成风控损失的可能。

- ⚡ **极致体积极客优化（~2.4 MB）**：
  - 剔除冗余依赖，完全使用 Android 原生标准库 `HttpURLConnection` 构建底层网络通信，零臃肿第三方网络框架。
  - 精简常用真机 ABI 架构（`arm64-v8a` + `armeabi-v7a`），单 DEX 结构。
  - 开启 R8 极限代码裁剪与资源压缩，体积精简超过 95%。

- 🎨 **现代化流畅交互**：
  - 纯 Kotlin + Jetpack Compose (Material 3) 编写。
  - 完整支持深色模式。
  - 完美适配软键盘平滑弹起避让（IME Insets），大屏小屏输入皆顺手。

---

## 🛠️ 技术栈

| 模块 | 技术选型 |
| :--- | :--- |
| **开发语言** | Kotlin 2.0 |
| **界面框架** | Jetpack Compose (Material 3) |
| **架构模式** | MVVM + StateFlow + Kotlin Coroutines |
| **本地存储** | AndroidX DataStore (Preferences + Kotlinx Serialization) |
| **网络层** | JDK 原生 `HttpURLConnection`（极致轻量、3秒超时严控、双端点故障转移） |
| **系统兼容** | Android 10 (API 29) 至 Android 16 (API 36) |
| **安装包体积** | 约 **2.4 MB** (Release APK) |

---

## 📥 安装与下载

前往项目的 [Releases 页面](../../releases) 或从 [GitHub Actions 构建产物](../../actions) 中下载最新版的 `app-release.apk`，安装到 Android 10 或更高版本的手机上即可使用。

---

## 🏗️ 源码构建

克隆仓库后使用 Gradle 即可直接构建：

```bash
# 克隆仓库
git clone https://github.com/Marcus1337X/ExitGuard.git
cd ExitGuard

# 编译生成 Release APK
./gradlew assembleRelease
```

构建产物将输出在 `app/build/outputs/apk/release/app-release.apk`。

---

## 📄 开源许可证

本项目基于 [Apache License 2.0](LICENSE) 协议开源。
