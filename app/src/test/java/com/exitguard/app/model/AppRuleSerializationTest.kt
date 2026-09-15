package com.exitguard.app.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AppRuleSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `serialize and deserialize AppRule list successfully`() {
        val rules = listOf(
            AppRule(
                packageName = "com.twitter.android",
                appName = "X",
                mode = CheckMode.IP_STRICT,
                allowedIps = setOf("1.1.1.1", "8.8.8.8")
            ),
            AppRule(
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                mode = CheckMode.COUNTRY,
                allowedCountries = setOf("US", "SG")
            )
        )

        val encoded = json.encodeToString(rules)
        val decoded = json.decodeFromString<List<AppRule>>(encoded)

        assertEquals(2, decoded.size)
        assertEquals("com.twitter.android", decoded[0].packageName)
        assertEquals(CheckMode.IP_STRICT, decoded[0].mode)
        assertEquals(setOf("1.1.1.1", "8.8.8.8"), decoded[0].allowedIps)

        assertEquals("org.telegram.messenger", decoded[1].packageName)
        assertEquals(CheckMode.COUNTRY, decoded[1].mode)
        assertEquals(setOf("US", "SG"), decoded[1].allowedCountries)
    }
}
