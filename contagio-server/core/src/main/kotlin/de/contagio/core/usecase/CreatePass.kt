package de.contagio.core.usecase

import de.brendamour.jpasskit.PKField
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.enums.PKDateStyle
import de.brendamour.jpasskit.passes.PKCoupon
import de.brendamour.jpasskit.passes.PKEventTicket
import de.brendamour.jpasskit.passes.PKGenericPass
import de.brendamour.jpasskit.passes.PKStoreCard
import de.brendamour.jpasskit.signing.PKFileBasedSigningUtil
import de.brendamour.jpasskit.signing.PKSigningInformationUtil
import de.contagio.core.domain.entity.*
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private val logger = LoggerFactory.getLogger(CreatePass::class.java)

// DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'+02:00'")


data class CreatePassParameter(
    val organisationName: String,
    val description: String,
    val logoText: String,
    val passType: PassType = PassType.GENERIC,
    val labelColor: String,
    val foregroundColor: String,
    val backgroundColor: String
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

        pass.labelColor = createPassParameter.labelColor
        pass.foregroundColor = createPassParameter.foregroundColor
        pass.backgroundColor = createPassParameter.backgroundColor

        pass.organizationName = createPassParameter.organisationName
        pass.description = createPassParameter.description
        pass.logoText = createPassParameter.logoText

        pass.addBarcode("$baseUrl/showpass?serialNumber=${passInfo.serialNumber}")

        val generic = when (createPassParameter.passType) {
            PassType.GENERIC -> PKGenericPass()
            PassType.COUPON -> PKCoupon()
            PassType.EVENT -> PKEventTicket()
            PassType.STORE -> PKStoreCard()
        }

        if (passInfo.issueStatus == IssueStatus.SIGNED)
            generic.headerFields = listOf(PKField("testType", null, "TESTTYPE_${passInfo.testType.name}"))
        else
            generic.headerFields = listOf(PKField("issueStatus", null, "ISSUESTATUS_${passInfo.issueStatus.name}"))

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
                "${baseUrl}/showpass?serialNumber=${passInfo.serialNumber}"
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
                val cpt = ContagioPassTemplate(passImage, passType)
                cpt.build()

                result = PKFileBasedSigningUtil().createSignedAndZippedPkPassArchive(pass, cpt, pkSigningInformation)
            } else {
                logger.debug("pass is NOT valid: ${pass.validationErrors}")
            }
        } catch (e: Exception) {
            logger.error("Error while creating signed pass payload", e)
        }

        return result
    }
}
