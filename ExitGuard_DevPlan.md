# ExitGuard Android 开发方案

## 1. 目标

开发 Android 10（API 29）及以上的安全应用启动器。

用户在 ExitGuard 内添加已安装 App，并为**每个 App 单独设置出口规则**。点击 App 时实时检测公网出口，符合规则才启动。

---

## 2. 功能

### 应用管理
- 读取手机中可启动的 App
- 搜索、添加、删除应用
- 首页统一启动已添加 App
- 每个 App 独立保存规则

### 检测模式

**严格 IP 模式（默认）**
- 每个 App 可配置多个允许出口 IP
- 当前出口 IP 必须与其中一个完全一致
- 支持手动添加 IP
- 支持“一键添加当前出口 IP”

**国家模式**
- 每个 App 可配置多个允许国家
- 使用 ISO 国家代码，如 `US`、`JP`、`SG`
- 当前出口国家符合即可放行

### 安全策略
以下情况全部禁止启动：
- 出口 IP / 国家不匹配
- 检测超时
- 网络异常
- API 返回异常
- 规则为空

不提供“仍然启动”。

---

## 3. 启动流程

```text
用户点击 App
      ↓
读取该 App 独立规则
      ↓
实时检测公网出口
      ↓
获取 IP + Country
      ↓
按规则判断
   ↙       ↘
通过        不通过
 ↓            ↓
启动 App     禁止启动
```

---

## 4. 出口检测

免费 API：

```text
GET https://ipwho.is/
```

读取字段：

```json
{
  "ip": "1.2.3.4",
  "success": true,
  "country": "United States",
  "country_code": "US"
}
```

内部模型：

```kotlin
data class ExitInfo(
    val ip: String,
    val countryCode: String
)
```

建议：
- 连接超时：3 秒
- 读取超时：3 秒
- `success != true`、HTTP 错误、解析失败均视为失败
- 第一版可预留备用检测接口

---

## 5. 数据模型

```kotlin
enum class CheckMode {
    IP_STRICT,
    COUNTRY
}

data class AppRule(
    val packageName: String,
    val appName: String,
    val mode: CheckMode = CheckMode.IP_STRICT,
    val allowedIps: Set<String> = emptySet(),
    val allowedCountries: Set<String> = emptySet()
)
```

判断逻辑：

```kotlin
fun isAllowed(
    rule: AppRule,
    exit: ExitInfo
): Boolean =
    when (rule.mode) {
        CheckMode.IP_STRICT ->
            exit.ip in rule.allowedIps

        CheckMode.COUNTRY ->
            exit.countryCode.uppercase() in rule.allowedCountries
    }
```

---

## 6. 页面

### 首页
- 显示已添加 App
- 显示每个 App 当前规则类型
- 点击 App → 检测 → 通过后自动启动
- 提供“添加应用”

### 应用配置页
- 切换 `严格 IP / 国家`
- 添加、删除允许 IP
- 一键添加当前出口 IP
- 添加、删除允许国家
- 显示当前出口 IP / 国家
- 显示当前规则是否匹配

### 添加应用页
- 列出可启动 App
- 显示名称、图标
- 支持搜索
- 多选添加

---

## 7. 技术栈

```text
Kotlin
Jetpack Compose
MVVM
DataStore
OkHttp

minSdk = 29
targetSdk = 36
compileSdk = 36
```

不需要：
- Root
- VpnService
- AccessibilityService
- 后台常驻
- 系统 Launcher

### 开发与构建环境

- 开发环境为 **Android 手机上的 Termux**，以环境现有工具进行必要的本地检查。
- 测试优先使用 **GitHub Actions**；凡是能在 GitHub Actions 中完成的自动化测试，都放到 CI 中执行。
- **正式编译必须使用 GitHub Actions** 完成，CI 负责构建 APK，并保留构建产物，确保构建过程可重复、与本地 Termux 环境解耦。

---

## 8. 第一版原则

- Android 10+
- 所有目标 App 从 ExitGuard 内统一启动
- 每个 App 独立配置
- 默认严格匹配出口 IP
- 检测异常或规则不匹配，一律禁止启动
