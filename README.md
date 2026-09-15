# ExitGuard 🛡️

Android 10（API 29）及以上的安全应用启动器。

ExitGuard 允许您在受控环境下管理和启动手机中的应用，并为每个应用单独配置出口公网 IP 或出口国家规则。每次启动目标应用前，ExitGuard 都会在毫秒级内检测公网出口；**仅当当前公网出口完全符合您为该应用配置的安全规则时，才放行启动**。

---

## 🌟 核心特性

- **严格 IP 模式（默认）**：
  - 为每个应用独立指定白名单公网出口 IP。
  - 启动前必须与其中一个完全一致。
  - 支持“一键添加当前出口 IP”，告别繁琐手动输入。

- **国家代码模式**：
  - 为应用配置允许的 ISO 国家代码（如 `US`、`JP`、`SG`、`HK`）。
  - 当前出口节点所属国家符合时方可启动。
  - 内置常见节点快捷选择标签。

- **零容忍安全策略**：
  - 出口 IP / 国家不匹配：**禁止启动**
  - 检测超时或无网络：**禁止启动**
  - 出口 API 返回异常：**禁止启动**
  - 规则未配置：**禁止启动**
  - **不提供任何“仍然启动”的绕过选项**，保障出口安全绝对受控。

- **轻量与隐私**：
  - 无需 Root 权限。
  - 不使用 VpnService，不篡改系统全局网络栈。
  - 无后台常驻服务，启动即检，省电可靠。

---

## 🛠️ 技术栈

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + StateFlow + Kotlin Coroutines
- **Storage**: AndroidX DataStore (Preferences + Kotlinx Serialization)
- **Network**: OkHttp 4 (3秒严控超时 + 备用降级节点)
- **Compatibility**: minSdk = 29 (Android 10), targetSdk = 36, compileSdk = 36

---

## 🚀 构建与持续集成

本项目通过 **GitHub Actions** 进行自动化单元测试与 APK 编译构建。

每当提交推送到 `main` 分支时，CI 流程将自动触发：
1. 运行核心安全评估与网络解析单元测试 (`./gradlew testDebugUnitTest`)
2. 构建可直接安装运行的 Debug APK (`./gradlew assembleDebug`)
3. 在 GitHub Actions 页面生成并归档构建产物 (`ExitGuard-debug-apk`)
