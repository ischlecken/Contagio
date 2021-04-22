package de.contagio.webapp.model

import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.entity.PassType

@Suppress("ArrayInDataClass")
data class CreatePassResponse(
    val passInfo: PassInfo,
    val passImage: PassImage,
    val pkPass: ByteArray? = null
)

