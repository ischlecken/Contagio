package de.contagio.webapp.model

import de.contagio.core.domain.entity.TestResultType
import java.time.LocalDateTime

data class UpdatePassRequest(
    val serialNumber: String,
    val testResult: TestResultType?,
    val validUntil: LocalDateTime?
)
