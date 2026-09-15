package com.exitguard.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleCheckerTest {

    @Test
    fun `strict IP mode passes when exit IP matches allowed IP`() {
        val rule = AppRule(
            packageName = "com.example.test",
            appName = "Test App",
            mode = CheckMode.IP_STRICT,
            allowedIps = setOf("1.2.3.4", "5.6.7.8")
        )
        val exit = ExitInfo(ip = "1.2.3.4", countryCode = "US")

        assertTrue(RuleChecker.isAllowed(rule, exit))
        val result = RuleChecker.evaluate(rule, exit)
        assertTrue(result is CheckResult.Allowed)
    }

    @Test
    fun `strict IP mode fails when exit IP does not match`() {
        val rule = AppRule(
            packageName = "com.example.test",
            appName = "Test App",
            mode = CheckMode.IP_STRICT,
            allowedIps = setOf("1.2.3.4")
        )
        val exit = ExitInfo(ip = "9.9.9.9", countryCode = "US")

        assertFalse(RuleChecker.isAllowed(rule, exit))
        val result = RuleChecker.evaluate(rule, exit)
        assertTrue(result is CheckResult.Denied)
        assertTrue((result as CheckResult.Denied).reason.contains("9.9.9.9"))
    }

    @Test
    fun `strict IP mode strictly denies when rule has no allowed IPs`() {
        val rule = AppRule(
            packageName = "com.example.test",
            appName = "Test App",
            mode = CheckMode.IP_STRICT,
            allowedIps = emptySet()
        )
        val exit = ExitInfo(ip = "1.2.3.4", countryCode = "US")

        assertFalse(RuleChecker.isAllowed(rule, exit))
        val result = RuleChecker.evaluate(rule, exit)
        assertTrue(result is CheckResult.Denied)
        assertTrue((result as CheckResult.Denied).reason.contains("未配置任何允许的出口 IP"))
    }

    @Test
    fun `strict IP mode handles whitespace in IP properly`() {
        val rule = AppRule(
            packageName = "com.example.test",
            appName = "Test App",
            mode = CheckMode.IP_STRICT,
            allowedIps = setOf(" 1.2.3.4 ")
        )
        val exit = ExitInfo(ip = "1.2.3.4", countryCode = "US")

        assertTrue(RuleChecker.isAllowed(rule, exit))
    }

    @Test
    fun `country mode passes when country code matches case-insensitively`() {
        val rule = AppRule(
            packageName = "com.example.test",
            appName = "Test App",
            mode = CheckMode.COUNTRY,
            allowedCountries = setOf("US", "JP")
        )
        val exit = ExitInfo(ip = "1.2.3.4", countryCode = "us", country = "United States")

        assertTrue(RuleChecker.isAllowed(rule, exit))
        val result = RuleChecker.evaluate(rule, exit)
        assertTrue(result is CheckResult.Allowed)
    }

    @Test
    fun `country mode fails when country code does not match`() {
        val rule = AppRule(
            packageName = "com.example.test",
            appName = "Test App",
            mode = CheckMode.COUNTRY,
            allowedCountries = setOf("SG", "JP")
        )
        val exit = ExitInfo(ip = "1.2.3.4", countryCode = "US", country = "United States")

        assertFalse(RuleChecker.isAllowed(rule, exit))
        val result = RuleChecker.evaluate(rule, exit)
        assertTrue(result is CheckResult.Denied)
    }

    @Test
    fun `country mode strictly denies when rule has no allowed countries`() {
        val rule = AppRule(
            packageName = "com.example.test",
            appName = "Test App",
            mode = CheckMode.COUNTRY,
            allowedCountries = emptySet()
        )
        val exit = ExitInfo(ip = "1.2.3.4", countryCode = "US")

        assertFalse(RuleChecker.isAllowed(rule, exit))
        val result = RuleChecker.evaluate(rule, exit)
        assertTrue(result is CheckResult.Denied)
        assertTrue((result as CheckResult.Denied).reason.contains("未配置任何允许的地区"))
    }

    @Test
    fun `evaluation fails when exit info is null`() {
        val rule = AppRule(
            packageName = "com.example.test",
            appName = "Test App",
            mode = CheckMode.IP_STRICT,
            allowedIps = setOf("1.2.3.4")
        )

        val result = RuleChecker.evaluate(rule, null)
        assertTrue(result is CheckResult.Denied)
        assertTrue((result as CheckResult.Denied).reason.contains("无法获取公网出口信息"))
    }
}
