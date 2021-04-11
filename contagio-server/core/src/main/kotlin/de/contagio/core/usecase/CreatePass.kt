package de.contagio.core.usecase

import de.brendamour.jpasskit.PKField
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.passes.PKGenericPass
import de.brendamour.jpasskit.signing.PKFileBasedSigningUtil
import de.brendamour.jpasskit.signing.PKPassTemplateFolder
import de.brendamour.jpasskit.signing.PKSigningInformationUtil
import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.entity.TestResultType
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.ByteBuffer
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val logger = LoggerFactory.getLogger(CreatePass::class.java)
private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)

class ContagioPassTemplate(
    pathToTemplateDirectory: String?,
    private val passImage: PassImage
) : PKPassTemplateFolder(pathToTemplateDirectory) {

    override fun provisionPassAtDirectory(tempPassDir: File?) {
        logger.debug("tempPassDir= ${tempPassDir}")

        super.provisionPassAtDirectory(tempPassDir)

        FileUtils.writeByteArrayToFile( File(tempPassDir,"thumbnail.png"), passImage.data)
    }
}

data class CreatePassParameter(
    val organisationName: String,
    val description: String,
    val logoText: String,
)

class CreatePass(
    private val teamIdentifier: String,
    private val passTypeIdentifier: String,
    private val authenticationToken: String,
    private val baseUrl: String
) {

    fun build(passInfo: PassInfo, createPassParameter: CreatePassParameter): PKPass {
        val pass = PKPass()

        pass.passTypeIdentifier = this.passTypeIdentifier
        pass.authenticationToken = this.authenticationToken
        pass.serialNumber = passInfo.serialNumber
        pass.teamIdentifier = this.teamIdentifier
        pass.webServiceURL = URL("$baseUrl/co_v1/wallet/")

        pass.foregroundColor = "rgb(255, 255, 255)"
        when (passInfo.testResult) {
            TestResultType.UNKNOWN -> pass.backgroundColor = "rgb(120, 120, 120)"
            TestResultType.NEGATIVE -> pass.backgroundColor = "rgb(31, 120, 31)"
            TestResultType.POSITIVE -> pass.backgroundColor = "rgb(120, 31, 31)"
        }

        pass.organizationName = createPassParameter.organisationName
        pass.description = createPassParameter.description
        pass.logoText = createPassParameter.logoText

        pass.addBarcode("$baseUrl/showpass?serialNumber=${passInfo.serialNumber}")

        val generic = PKGenericPass()
        generic.primaryFields = listOf(PKField("TestResult", null, "testresult_${passInfo.testResult.name}"))
        generic.auxiliaryFields = listOf(
            PKField("UserId", "USERID", passInfo.person.fullName),
            PKField("ValidUntil", "VALIDUNTIL", passInfo.validUntil?.format(dateTimeFormatter))
        )

        pass.generic = generic

        //pass.expirationDate = Date.valueOf(passInfo.validUntil?.toLocalDate())
        //pass.addLocation(37.33182, -122.03118)

        logger.debug("CreatePass() pass=$pass")

        return pass
    }

    fun buildSignedPassPayload(
        resourcesBaseDirPath: String,
        keyName: String,
        privateKeyPassword: String,
        templateName: String,
        passImage: PassImage,
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

                logger.debug("using pass templates from $pathToTemplateDirectory...")

                result = PKFileBasedSigningUtil().createSignedAndZippedPkPassArchive(
                    pass,
                    ContagioPassTemplate(pathToTemplateDirectory, passImage),
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
