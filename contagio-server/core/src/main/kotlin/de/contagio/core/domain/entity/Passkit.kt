package de.contagio.core.domain.entity

import de.contagio.core.domain.port.IUIDGenerator
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class DeviceInfo(
    @Id val deviceLibraryIdentifier: String,
    val pushToken: String,
    val created: LocalDateTime = LocalDateTime.now()
)

data class RegistrationInfo(
    @Id val id: String,
    val deviceLibraryIdentifier: String,
    val serialNumber: String,
    val created: LocalDateTime = LocalDateTime.now()
)

