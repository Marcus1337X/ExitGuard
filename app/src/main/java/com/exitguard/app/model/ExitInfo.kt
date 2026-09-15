package com.exitguard.app.model

import kotlinx.serialization.Serializable

@Serializable
data class ExitInfo(
    val ip: String,
    val countryCode: String,
    val country: String = ""
)
