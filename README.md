# ExitGuard 🛡️

**专为严苛网络环境打造的 Android 应用安全启动台 / 出口网络哨兵。**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%2010%2B-green.svg)](https://developer.android.com)
[![Package Size](https://img.shields.io/badge/Package%20Size-2.4%20MB-success.svg)]()

ExitGuard 允许您在受控环境下集中管理与启动手机中的关键应用。支持为每个应用独立绑定出口 IP（IPv4 / IPv6）或出口地区白名单。**启动应用前毫秒级校验当前公网出口，仅当完全符合安全规则才放行启动，否则坚决阻断。**

---

## 🎯 解决的痛点

- **防串节点，避免风控封号**：日常切换代理节点处理事务后，极易忘记切回固定节点而误点开对 IP 极度敏感的 AI 服务（如 Claude 等）导致封号。ExitGuard 在启动瞬间拦截误触。
- **专属浏览器 / Web 工作台隔离**：为特定浏览器绑定专属出口地区或 IP，专用于访问特定 AI/服务的网页端，确保网络环境始终如一。
- **集中启动，告别繁琐翻找**：作为安全聚合工作台，受保护的应用集中陈列并统一启动，不局限于 AI，适用于任何需要出口一致性的应用。

---

## ⚠️ 防护边界与隐私透明度

- **防护边界（诚实公开）**：ExitGuard 专注于**启动前校验**，能杜绝“忘记切节点而误启动”；若应用已放行启动后，您在通知栏或后台手动切换节点，则不在防护范围内。
- **透明无隐私风险**：出口检测直接请求 **Cloudflare 官方公开追踪端点**（`https://www.cloudflare.com/cdn-cgi/trace` / `https://1.1.1.1/cdn-cgi/trace`），不经过任何第三方服务器，零日志收集。
- **绿色无感**：无需 Root 权限，不占用系统的 VPN 槽位（可与所有代理客户端并存），无后台常驻进程。

---

## 🔒 核心功能

- **严格 IP 模式**：
  - 支持 **IPv4** 与 **IPv6**，内置规范化等价匹配（兼容压缩/展开式与大小写）。
  - 支持一键添加当前公网出口 IP 或手动录入多条白名单。
- **出口地区模式**：
  - 支持配置允许的 ISO 国家/地区两位代码（如 `US`、`JP`、`SG`、`HK` 等）。
  - 支持一键添加当前出口地区或手动输入维护列表。
- **零容忍拦截策略**：
  - IP/地区不匹配、网络超时或未配置规则时坚决阻断启动。
  - **不提供任何“跳过警告”的绕过选项**，保障出口安全绝对受控。

---

## 🛠️ 技术规格

| 项目 | 说明 |
| :--- | :--- |
| **系统要求** | Android 10 (API 29) ~ Android 16 (API 36) |
| **技术架构** | Kotlin 2.0 · Jetpack Compose (Material 3) · DataStore |
| **网络层** | JDK 原生 `HttpURLConnection`（无第三方网络库依赖，双端点容灾） |
| **安装包体积** | 约 **2.4 MB**（单 DEX，精简架构 `arm64-v8a` / `armeabi-v7a`） |

---

## 📥 获取与构建

- **下载 APK**：前往 [Releases 页面](../../releases) 或 [GitHub Actions](../../actions) 下载最新 `app-release.apk`。
- **源码构建**：
  ```bash
  git clone https://github.com/Marcus1337X/ExitGuard.git
  cd ExitGuard
  ./gradlew assembleRelease
  ```

---

## 📄 开源许可证

本项目基于 [Apache License 2.0](LICENSE) 开源。
