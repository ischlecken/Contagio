package de.contagio.webapp.model

import de.contagio.core.domain.entity.TestResultType

data class CreatePassRequest(val userId: String, val testResult: TestResultType)
