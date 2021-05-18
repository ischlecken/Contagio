package de.contagio.core.domain.entity

import de.brendamour.jpasskit.PKPass


@Suppress("ArrayInDataClass")
data class PassSigningInfo(
    val keystore: ByteArray,
    val keystorePassword: String,
    val appleWWDRCA: ByteArray
)


@Suppress("ArrayInDataClass")
data class PassBuilderInfo(
    val passInfoEnvelope: PassInfoEnvelope,
    val passInfo: PassInfo,
    val passImage: ByteArray,
    val teststation: Teststation,
    val tester: Tester,
    val passSigningInfo: PassSigningInfo,
)


@Suppress("ArrayInDataClass")
data class PassBuilderResult(
    val pkpass: PKPass,
    val pass: ByteArray
)
