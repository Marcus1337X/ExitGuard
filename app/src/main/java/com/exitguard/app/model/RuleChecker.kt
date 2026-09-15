package com.exitguard.app.model

sealed interface CheckResult {
    data class Allowed(val exitInfo: ExitInfo) : CheckResult
    data class Denied(val exitInfo: ExitInfo?, val reason: String) : CheckResult
    data class Error(val message: String, val cause: Throwable? = null) : CheckResult
}

object RuleChecker {

    fun isAllowed(rule: AppRule, exit: ExitInfo): Boolean {
        return evaluate(rule, exit) is CheckResult.Allowed
    }

    fun evaluate(rule: AppRule, exit: ExitInfo?): CheckResult {
        if (exit == null) {
            return CheckResult.Denied(null, "无法获取公网出口信息，禁止启动")
        }

        return when (rule.mode) {
            CheckMode.IP_STRICT -> {
                val cleanedAllowedIps = rule.allowedIps
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()

                if (cleanedAllowedIps.isEmpty()) {
                    CheckResult.Denied(exit, "未配置任何允许的出口 IP 规则，安全起见禁止启动")
                } else if (cleanedAllowedIps.contains(exit.ip.trim())) {
                    CheckResult.Allowed(exit)
                } else {
                    CheckResult.Denied(
                        exit,
                        "当前出口 IP (${exit.ip}) 不在允许列表中"
                    )
                }
            }

            CheckMode.COUNTRY -> {
                val cleanedAllowedCountries = rule.allowedCountries
                    .map { it.trim().uppercase() }
                    .filter { it.isNotEmpty() }
                    .toSet()

                val currentCountryCode = exit.countryCode.trim().uppercase()

                if (cleanedAllowedCountries.isEmpty()) {
                    CheckResult.Denied(exit, "未配置任何允许的地区规则，安全起见禁止启动")
                } else if (cleanedAllowedCountries.contains(currentCountryCode)) {
                    CheckResult.Allowed(exit)
                } else {
                    val displayCountry = if (exit.country.isNotEmpty()) {
                        "${exit.country} ($currentCountryCode)"
                    } else {
                        currentCountryCode
                    }
                    CheckResult.Denied(
                        exit,
                        "当前出口地区 ($displayCountry) 不在允许列表中"
                    )
                }
            }
        }
    }
}
