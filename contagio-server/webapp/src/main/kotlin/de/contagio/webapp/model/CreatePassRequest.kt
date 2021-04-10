package de.contagio.webapp.model

import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfo

data class CreatePassRequest(
    val passInfo: PassInfo,
    val passImage: PassImage
)
