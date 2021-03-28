package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class PassInfo(
    @Id val serialNumber: String,
    val userId: String,
    val created: LocalDateTime = LocalDateTime.now(),
    val validUntil: LocalDateTime = LocalDateTime.now().plusHours(12)
)
