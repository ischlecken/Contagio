package de.contagio.webapp.model

import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfoEnvelope

@Suppress("ArrayInDataClass")
data class CreatePassResponse(
    val passInfoEnvelope: PassInfoEnvelope,
    val passImage: PassImage,
    val pkPass: ByteArray? = null
)

