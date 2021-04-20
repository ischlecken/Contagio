package de.contagio.core.usecase

import de.brendamour.jpasskit.PKField
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.enums.PKDateStyle
import de.brendamour.jpasskit.passes.PKCoupon
import de.brendamour.jpasskit.passes.PKEventTicket
import de.brendamour.jpasskit.passes.PKGenericPass
import de.brendamour.jpasskit.passes.PKStoreCard
import de.brendamour.jpasskit.signing.PKFileBasedSigningUtil
import de.brendamour.jpasskit.signing.PKPassTemplateFolder
import de.brendamour.jpasskit.signing.PKSigningInformationUtil
import de.contagio.core.domain.entity.*
import net.coobird.thumbnailator.Thumbnails
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.imageio.ImageIO

private val logger = LoggerFactory.getLogger(CreatePass::class.java)

// DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'+02:00'")

class ContagioPassTemplate(
    pathToTemplateDirectory: String?,
    private val passImage: PassImage,
    private val passType: PassType
) : PKPassTemplateFolder(pathToTemplateDirectory) {

    override fun provisionPassAtDirectory(tempPassDir: File?) {
        logger.debug("tempPassDir= $tempPassDir")

        super.provisionPassAtDirectory(tempPassDir)

        val inputImage: BufferedImage = ImageIO.read(ByteArrayInputStream(passImage.data))

        when (passType) {
            PassType.COUPON -> {
                createImages(tempPassDir, inputImage, 375, 144, "strip")
            }
            PassType.EVENT -> {
                createImages(tempPassDir, inputImage, 90, 90, "thumbnail")
                createImages(tempPassDir, inputImage, 180, 220, "background")
            }
            else -> {
                createImages(tempPassDir, inputImage, 90, 90, "thumbnail")
            }
        }
    }

    private fun createImages(tempPassDir: File?, inputImage: BufferedImage, width: Int, height: Int, name: String) {
        val resizedImage = resizeImage(inputImage, width, height)
        val resizedImage2x = resizeImage(inputImage, 2 * width, 2 * height)

        FileUtils.writeByteArrayToFile(File(tempPassDir, "$name.png"), resizedImage)
        FileUtils.writeByteArrayToFile(File(tempPassDir, "$name@2x.png"), resizedImage2x)
    }


    @Throws(java.lang.Exception::class)
    fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): ByteArray? {
        val outputStream = ByteArrayOutputStream()

        Thumbnails.of(originalImage)
            .size(targetWidth, targetHeight)
            .outputFormat("PNG")
            .outputQuality(1.0)
            .toOutputStream(outputStream)

        val result = outputStream.toByteArray()

        logger.debug("resizeImage($targetWidth,$targetHeight):${result.size}")

        return result
    }

}

data class CreatePassParameter(
    val organisationName: String,
    val description: String,
    val logoText: String,
    val passType: PassType = PassType.GENERIC
)

class CreatePass(
    private val teamIdentifier: String,
    private val passTypeIdentifier: String,
    private val authenticationToken: String,
    private val baseUrl: String
) {

    fun build(passInfo: PassInfo, teststation: Teststation, createPassParameter: CreatePassParameter): PKPass {
        val pass = PKPass()

        val validUntilFormatted = passInfo.validUntil?.format(dateTimeFormatter)

        pass.passTypeIdentifier = this.passTypeIdentifier
        pass.teamIdentifier = this.teamIdentifier
        pass.serialNumber = passInfo.serialNumber
        pass.webServiceURL = URL("$baseUrl/co_v1/wallet/")
        pass.authenticationToken = this.authenticationToken
        pass.isSharingProhibited = true

        if (passInfo.validUntil != null)
            pass.expirationDate = Date.from(passInfo.validUntil.atZone(ZoneId.systemDefault()).toInstant())

        pass.labelColor = "rgb(5, 175, 190)"
        pass.foregroundColor = "rgb(255, 255, 255)"
        pass.backgroundColor = "rgb(208, 38, 0)"

        pass.organizationName = createPassParameter.organisationName
        pass.description = createPassParameter.description
        pass.logoText = "LOGO_TESTTYPE"

        pass.addBarcode("$baseUrl/showpass?serialNumber=${passInfo.serialNumber}")

        val generic = when (createPassParameter.passType) {
            PassType.GENERIC -> PKGenericPass()
            PassType.COUPON -> PKCoupon()
            PassType.EVENT -> PKEventTicket()
            PassType.STORE -> PKStoreCard()
        }

        generic.headerFields = listOf(PKField("testType", null, "TESTTYPE_${passInfo.testType.name}"))
        when (createPassParameter.passType) {
            PassType.COUPON -> generic.primaryFields = listOf(
                PKField("testResult", "TESTRESULT_${passInfo.testResult.name}", "")
            )
            else -> generic.primaryFields = listOf(
                PKField("fullName", "FULLNAME", passInfo.person.fullName)
            )
        }

        val secondaryFields = mutableListOf<PKField>()
        if (createPassParameter.passType == PassType.COUPON)
            secondaryFields.add(PKField("fullName", "FULLNAME", passInfo.person.fullName))
        else
            secondaryFields.add(PKField("testResult", "TESTRESULT", "TESTRESULT_${passInfo.testResult.name}"))

        if (validUntilFormatted != null && passInfo.testResult != TestResultType.UNKNOWN) {
            val validUntilField = PKField("validUntil", "VALIDUNTIL", validUntilFormatted)
            validUntilField.dateStyle = PKDateStyle.PKDateStyleMedium
            validUntilField.timeStyle = PKDateStyle.PKDateStyleShort
            validUntilField.isRelative = true

            secondaryFields.add(validUntilField)
        }

        val auxiliaryFields = mutableListOf<PKField>()
        auxiliaryFields.add(PKField("teststation", "TESTSTATION", teststation.name))
        auxiliaryFields.add(PKField("teststationAddress", "TESTSTATIONADDRESS", teststation.address.fullAddress))

        val backFields = mutableListOf<PKField>()
        if (!passInfo.person.email.isNullOrEmpty())
            backFields.add(PKField("email", "EMAIL", passInfo.person.email))

        if (!passInfo.person.phoneNo.isNullOrEmpty())
            backFields.add(PKField("phoneNo", "PHONENO", passInfo.person.phoneNo))

        backFields.add(PKField("teststationId", "TESTSTATIONID", passInfo.teststationId))
        backFields.add(PKField("testerId", "TESTERID", passInfo.testerId))

        if (validUntilFormatted != null) {
            val validUntilField1 = PKField("validUntil1", "VALIDUNTIL", validUntilFormatted)
            validUntilField1.dateStyle = PKDateStyle.PKDateStyleMedium
            validUntilField1.timeStyle = PKDateStyle.PKDateStyleShort
            backFields.add(validUntilField1)
        }

        backFields.add(
            PKField(
                "showPassUrl",
                "SHOWPASSURL",
                "https://efeu.local:13013/showpass?serialNumber=${passInfo.serialNumber}"
            )
        )

        backFields.add(PKField("terms", "TERMSCONDITIONS", "TERMSCONDITIONS_VALUE"))

        generic.secondaryFields = secondaryFields
        generic.auxiliaryFields = auxiliaryFields
        generic.backFields = backFields

        when (createPassParameter.passType) {
            PassType.GENERIC -> pass.generic = generic
            PassType.COUPON -> pass.coupon = generic as PKCoupon?
            PassType.EVENT -> pass.eventTicket = generic as PKEventTicket?
            PassType.STORE -> pass.storeCard = generic as PKStoreCard?
        }

        logger.debug("CreatePass() pass=$pass")

        return pass
    }

    fun buildSignedPassPayload(
        resourcesBaseDirPath: String,
        keyName: String,
        privateKeyPassword: String,
        templateName: String,
        passImage: PassImage,
        passType: PassType,
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
                    ContagioPassTemplate(pathToTemplateDirectory, passImage, passType),
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
