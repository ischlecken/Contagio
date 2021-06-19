package de.contagio.core.domain.entity

import java.time.Instant

data class DeviceToken(
    val deviceToken: String,
    val serialNumber: String,
    val created: Instant = Instant.now(),
    val lastUsed: Instant? = null,
    val id: String? = null
)
