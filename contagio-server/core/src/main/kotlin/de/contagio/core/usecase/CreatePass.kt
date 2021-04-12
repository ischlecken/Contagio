package de.contagio.core.usecase

import de.brendamour.jpasskit.PKField
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.enums.PKDateStyle
import de.brendamour.jpasskit.passes.PKEventTicket
import de.brendamour.jpasskit.signing.PKFileBasedSigningUtil
import de.brendamour.jpasskit.signing.PKPassTemplateFolder
import de.brendamour.jpasskit.signing.PKSigningInformationUtil
import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.entity.TestResultType
import de.contagio.core.domain.entity.TestType
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

private val logger = LoggerFactory.getLogger(CreatePass::class.java)
private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
private val dateTimeFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'+02:00'")
// 2013-08-10T19:30-06:00

class ContagioPassTemplate(
    pathToTemplateDirectory: String?,
    private val passImage: PassImage
) : PKPassTemplateFolder(pathToTemplateDirectory) {

    override fun provisionPassAtDirectory(tempPassDir: File?) {
        logger.debug("tempPassDir= ${tempPassDir}")

        super.provisionPassAtDirectory(tempPassDir)

        FileUtils.writeByteArrayToFile(File(tempPassDir, "thumbnail.png"), passImage.data)
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

        val validUntilFormatted = passInfo.validUntil?.format(dateTimeFormatter1)

        pass.passTypeIdentifier = this.passTypeIdentifier
        pass.teamIdentifier = this.teamIdentifier
        pass.serialNumber = passInfo.serialNumber
        pass.webServiceURL = URL("$baseUrl/co_v1/wallet/")
        pass.authenticationToken = this.authenticationToken

        if (passInfo.validUntil != null)
            pass.expirationDate = Date.from(passInfo.validUntil.atZone(ZoneId.systemDefault()).toInstant());

        pass.foregroundColor = "rgb(255, 255, 255)"
        pass.labelColor = "rgb(242, 55, 55)"
        when (passInfo.testResult) {
            TestResultType.UNKNOWN -> pass.backgroundColor = "rgb(242, 121, 55)"
            TestResultType.NEGATIVE -> pass.backgroundColor = "rgb(105, 150, 17)"
            TestResultType.POSITIVE -> pass.backgroundColor = "rgb(242, 55, 55)"
        }

        pass.organizationName = createPassParameter.organisationName
        pass.description = createPassParameter.description
        pass.logoText = "LOGO_TESTTYPE_${passInfo.testType.name}"

        pass.addBarcode("$baseUrl/showpass?serialNumber=${passInfo.serialNumber}")

        //val generic = PKGenericPass()
        //val generic = PKCoupon()
        val generic = PKEventTicket()

        if (passInfo.testType == TestType.VACCINATION)
            generic.headerFields = listOf(PKField("testType", null, "VACCINATION"))
        else
            generic.headerFields = listOf(PKField("testResult", "TESTRESULT", "TESTRESULT_${passInfo.testResult.name}"))

        generic.primaryFields = listOf(
            PKField("fullName", "FULLNAME", passInfo.person.fullName),
        )

        logger.debug("validUntil=${validUntilFormatted}")

        val validUntilField = PKField("validUntil", "VALIDUNTIL", validUntilFormatted)
        validUntilField.dateStyle = PKDateStyle.PKDateStyleMedium
        validUntilField.timeStyle = PKDateStyle.PKDateStyleShort
        validUntilField.isRelative = true

        generic.secondaryFields = listOf(validUntilField)

        val auxiliaryFields = mutableListOf<PKField>()
        auxiliaryFields.add(PKField("teststationId", "TESTSTATIONID", passInfo.teststationId))

        val backFields = mutableListOf<PKField>()
        if (!passInfo.person.email.isNullOrEmpty())
            backFields.add(PKField("email", "EMAIL", passInfo.person.email))

        if (!passInfo.person.phoneNo.isNullOrEmpty())
            backFields.add(PKField("phoneNo", "PHONENO", passInfo.person.phoneNo))

        backFields.add(PKField("testerId", "TESTERID", passInfo.testerId))

        val validUntilField1 =
            PKField("validUntil1", "VALIDUNTIL", validUntilFormatted)
        validUntilField1.dateStyle = PKDateStyle.PKDateStyleMedium
        validUntilField1.timeStyle = PKDateStyle.PKDateStyleShort
        backFields.add(validUntilField1)

        backFields.add(
            PKField(
                "showPassUrl",
                "SHOWPASSURL",
                "https://efeu.local:13013/showpass?serialNumber=${passInfo.serialNumber}"
            )
        )

        backFields.add(PKField("terms", "TERMSCONDITIONS", "TERMSCONDITIONS_VALUE"))

        generic.auxiliaryFields = auxiliaryFields
        generic.backFields = backFields

        //pass.generic = generic
        //pass.coupon = generic
        pass.eventTicket = generic

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
