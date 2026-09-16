# ExitGuard 🛡️

**Android Pre-Launch Exit Network Verification Launcher.**

ExitGuard verifies the current public network exit (IP / Region) before launching designated apps, enforcing granular rules defined independently for each app.

[English](README_EN.md) | [简体中文](README.md)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Android-10%2B-green.svg)](https://developer.android.com)

## Key Features

- **Per-App Independent Configuration**
- **Strict IP Mode (Default)**
  - Supports IPv4 and IPv6 address matching
  - Supports multiple allowed IPs
  - One-tap quick addition of current exit IP
- **Country / Region Mode**
  - Uses ISO 3166-1 alpha-2 country codes (e.g. `US`, `JP`, `SG`)
  - Supports multiple allowed regions
- **Fail-Closed**
  - Blocks app launch immediately on exit mismatch, detection failure, timeout, or empty rule set
  - No bypass or "launch anyway" options provided
- **Lightweight & Clean**
  - No Root required
  - Does not occupy Android VPN slot
  - No background persistent services

## How It Works

```text
Tap App in ExitGuard
        ↓
Real-time Public Exit Detection
        ↓
Load Per-App Security Rule
        ↓
   Strict IP / Country
     ↙       ↘
  Matched   Mismatch
    ↓           ↓
Launch App  Block Launch
```

Exit detection directly queries Cloudflare official public trace endpoints:

```text
https://www.cloudflare.com/cdn-cgi/trace
https://1.1.1.1/cdn-cgi/trace
```

A single check returns the exit IP and country/region code currently in use for that connection.

## Protection Boundaries

ExitGuard is a **pre-launch validation utility**, not a system-level network firewall.

- Verification is only triggered when launching the target app from within ExitGuard.
- Launching directly from the system home screen, notifications, recent apps, or third-party apps will not route through ExitGuard.
- Once an app is validated and launched, ExitGuard does not continuously monitor or intercept runtime proxy or network switches.

## Privacy

ExitGuard does not collect, record, or upload app rules, detection logs, or user analytics.

All exit detection queries connect directly to Cloudflare official endpoints with zero private relay servers.

## System Requirements

- Android 10 (API 29) or higher

## Download & Build

Download the official signed release APK from [Releases](../../releases).

> **Note**: Artifacts produced by public repo CI runs are strictly for compilation verification (using a debug keystore). Always obtain official production packages from the Releases link above.

Build from source:

```bash
git clone https://github.com/Marcus1337X/ExitGuard.git
cd ExitGuard
./gradlew assembleRelease
```

## License

[Apache License 2.0](LICENSE)
