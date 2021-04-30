package de.contagio.webapp.model

import de.contagio.core.domain.entity.TestResultType
import java.time.Instant

data class UpdatePassRequest(
    val serialNumber: String,
    val testResult: TestResultType?,
    val validUntil: Instant?
)
