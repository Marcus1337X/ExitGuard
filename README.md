# ExitGuard 🛡️

**Android 应用出口校验启动器。**

ExitGuard 用于在启动指定应用前检查当前公网出口，并按每个应用独立配置的规则决定是否放行。

[English](README_EN.md) | [简体中文](README.md)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Android-10%2B-green.svg)](https://developer.android.com)

## 核心功能

- **每个应用独立配置**
- **严格 IP 模式（默认）**
  - 支持 IPv4 / IPv6 地址匹配
  - 支持多个允许 IP
  - 支持一键添加当前出口 IP
- **匹配地区模式**
  - 使用 ISO 3166-1 alpha-2 两位代码，例如 `US`、`JP`、`SG`
  - 支持多个允许地区
- **Fail-Closed**
  - 出口不匹配、检测失败、网络超时或规则为空时直接阻止启动
  - 不提供跳过检查继续启动的选项
- **轻量**
  - 无需 Root
  - 不占用系统 VPN 槽位
  - 无后台常驻

## 工作方式

```text
在 ExitGuard 中点击应用
        ↓
实时检测公网出口
        ↓
读取该应用独立规则
        ↓
   严格 IP / 匹配地区
     ↙       ↘
   通过       不通过
    ↓           ↓
 启动应用     阻止启动
```

出口检测直接请求 Cloudflare 官方公开 Trace 端点：

```text
https://www.cloudflare.com/cdn-cgi/trace
https://1.1.1.1/cdn-cgi/trace
```

单次检测返回当前连接实际使用的出口 IP 与国家 / 地区代码。

## 防护边界

ExitGuard 是**启动前校验工具**，不是系统级防火墙。

- 只有从 ExitGuard 内启动目标应用时才会执行检查。
- 从系统桌面、通知、最近任务或其他应用直接进入目标应用，不会经过 ExitGuard。
- 目标应用放行启动后，如果之后切换代理节点或网络环境，ExitGuard 不会持续监控或拦截。

## 隐私

ExitGuard 本身不收集或上传应用规则、检测历史或用户日志。

出口检测请求会直接发送到 Cloudflare 官方服务，不经过 ExitGuard 自建中转服务器。

## 系统要求

- Android 10 / API 29 及以上

## 下载与构建

从 [Releases](../../releases) 下载最新 APK。

源码构建：

```bash
git clone https://github.com/Marcus1337X/ExitGuard.git
cd ExitGuard
./gradlew assembleRelease
```

## License

[Apache License 2.0](LICENSE)
