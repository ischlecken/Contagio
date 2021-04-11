package de.contagio.core.usecase

import de.brendamour.jpasskit.PKField
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.passes.PKGenericPass
import de.brendamour.jpasskit.signing.PKFileBasedSigningUtil
import de.brendamour.jpasskit.signing.PKSigningInformationUtil
import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfo
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val logger = LoggerFactory.getLogger(CreatePass::class.java)
private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)

class CreatePass(
    private val teamIdentifier: String,
    private val passTypeIdentifier: String,
    private val authenticationToken: String,
    private val baseUrl: String
) {

    fun build(passInfo: PassInfo, passImage: PassImage): PKPass {
        val pass = PKPass()
        pass.passTypeIdentifier = this.passTypeIdentifier
        pass.authenticationToken = this.authenticationToken
        pass.serialNumber = passInfo.serialNumber
        pass.teamIdentifier = this.teamIdentifier
        pass.webServiceURL = URL("$baseUrl/co_v1/wallet/")

        pass.foregroundColor = "rgb(255, 255, 255)"
        pass.backgroundColor = "rgb(31, 120, 31)"
        pass.organizationName = "contagio"
        pass.description = "Der Pass in die Freiheit"
        pass.logoText = "Das Tübinger Modell"

        pass.addBarcode("$baseUrl/co_v1/pass/info/${passInfo.serialNumber}")

        val generic = PKGenericPass()
        generic.primaryFields = listOf(PKField("TestResult", null, passInfo.testResult.display))
        generic.auxiliaryFields = listOf(
            PKField("UserId", "USERID", passInfo.person.fullName),
            PKField("ValidUntil", "VALIDUNTIL", passInfo.validUntil.format(dateTimeFormatter))
        )

        pass.generic = generic

        pass.addLocation(37.33182, -122.03118)

        logger.debug("CreatePass() pass=$pass")

        return pass
    }

    fun buildSignedPassPayload(
        resourcesBaseDirPath: String,
        keyName: String,
        privateKeyPassword: String,
        templateName: String,
        pass: PKPass
    ): ByteArray? {
        val appleWWDRCA = "$resourcesBaseDirPath/certs/AppleWWDRCA.cer"
        val privateKeyPath = "$resourcesBaseDirPath/certs/$keyName.p12"
        var result: ByteArray? = null
        try {
            val pkSigningInformation =
                PKSigningInformationUtil().loadSigningInformationFromPKCS12AndIntermediateCertificate(
                    privateKeyPath,
                    privateKeyPassword,
                    appleWWDRCA
                )

            if (pass.isValid) {
                val pathToTemplateDirectory = "$resourcesBaseDirPath/templates/$templateName"

                result = PKFileBasedSigningUtil().createSignedAndZippedPkPassArchive(
                    pass,
                    pathToTemplateDirectory,
                    pkSigningInformation
                )
            } else {
                logger.debug("pass is NOT valid: ${pass.validationErrors}")
            }
        } catch (e: Exception) {
            logger.error("Error while creating signed pass payload", e)
        }

        return result
    }
}
