package de.contagio.core.domain.entity

import de.brendamour.jpasskit.PKPass
import java.io.InputStream


data class PassSigningInfo(
    val keystore: InputStream,
    val keystorePassword: String,
    val appleWWDRCA: InputStream
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
