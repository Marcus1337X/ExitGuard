package com.exitguard.app.model

import kotlinx.serialization.Serializable

@Serializable
data class AppRule(
    val packageName: String,
    val appName: String,
    val mode: CheckMode = CheckMode.IP_STRICT,
    val allowedIps: Set<String> = emptySet(),
    val allowedCountries: Set<String> = emptySet()
)
