package com.exitguard.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class CheckMode {
    IP_STRICT,
    COUNTRY
}
