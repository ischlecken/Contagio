package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.Instant

data class DeviceInfo(
    @Id val deviceLibraryIdentifier: String,
    val pushToken: String,
    val created: Instant = Instant.now()
)

data class RegistrationInfo(
    @Id val id: String,
    val deviceLibraryIdentifier: String,
    val serialNumber: String,
    val created: Instant = Instant.now()
)

