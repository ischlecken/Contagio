package de.contagio.webapp.model

import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfo

@Suppress("ArrayInDataClass")
data class CreatePassResponse(
    val passInfo: PassInfo,
    val passImage: PassImage,
    val pkPass: ByteArray? = null
)
