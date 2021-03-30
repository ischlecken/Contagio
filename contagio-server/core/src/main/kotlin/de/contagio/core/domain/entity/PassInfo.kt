package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

enum class TestResultType(val display: String) {
    UNKNOWN("unbekannt"), POSITIVE("positiv"), NEGATIVE("negativ")
}

data class PassInfo(
    @Id val serialNumber: String,
    val userId: String,
    val testResult: TestResultType,
    val created: LocalDateTime = LocalDateTime.now(),
    val validUntil: LocalDateTime = LocalDateTime.now().plusHours(12)
)
