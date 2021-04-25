package de.contagio.core.domain.entity

import de.brendamour.jpasskit.PKPass
import java.io.InputStream

data class PassCoreInfo(
    val teamIdentifier: String,
    val passTypeIdentifier: String,
    val authenticationToken: String,
    val baseUrl: String,
    val organisationName: String,
)


data class PassSigningInfo(
    val keystore: InputStream,
    val keystorePassword: String,
    val appleWWDRCA: InputStream
)


data class PassBuilderInfo(
    val passCoreInfo: PassCoreInfo,
    val passSigningInfo: PassSigningInfo,
    val passImage: PassImage,
    val passInfo: PassInfo,
    val teststation: Teststation,
    val tester: Tester,

)


@Suppress("ArrayInDataClass")
data class PassBuilderResult(
    val pkpass: PKPass,
    val pass: ByteArray?
)
