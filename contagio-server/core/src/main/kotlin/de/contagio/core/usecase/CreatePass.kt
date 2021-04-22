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

class CreatePass(
    private val teamIdentifier: String,
    private val passTypeIdentifier: String,
    private val authenticationToken: String,
    private val baseUrl: String
) {

    fun build(createPassParameter: CreatePassParameter): PKPass {
        val pass = initCorePass(createPassParameter)

        val generic = when (createPassParameter.passType) {
            PassType.GENERIC -> PKGenericPass()
            PassType.COUPON -> PKCoupon()
            PassType.EVENT -> PKEventTicket()
            PassType.STORE -> PKStoreCard()
        }

        generic.headerFields = createHeaderFields(createPassParameter)
        generic.primaryFields = createPrimaryFields(createPassParameter)
        generic.secondaryFields = createSecondaryFields(createPassParameter)
        generic.auxiliaryFields = createAuxiliaryFields(createPassParameter)
        generic.backFields = createBackfields(createPassParameter)

        when (createPassParameter.passType) {
            PassType.GENERIC -> pass.generic = generic
            PassType.COUPON -> pass.coupon = generic as PKCoupon?
            PassType.EVENT -> pass.eventTicket = generic as PKEventTicket?
            PassType.STORE -> pass.storeCard = generic as PKStoreCard?
        }

        logger.debug("CreatePass() pass=$pass")

        return pass
    }

    private fun initCorePass(createPassParameter: CreatePassParameter): PKPass {
        val pass = PKPass()

        pass.passTypeIdentifier = this.passTypeIdentifier
        pass.teamIdentifier = this.teamIdentifier
        pass.serialNumber = createPassParameter.passInfo.serialNumber
        pass.webServiceURL = URL("$baseUrl/co_v1/wallet/")
        pass.authenticationToken = this.authenticationToken
        pass.isSharingProhibited = true

        if (createPassParameter.passInfo.validUntil != null)
            pass.expirationDate =
                Date.from(createPassParameter.passInfo.validUntil.atZone(ZoneId.systemDefault()).toInstant())

        pass.labelColor = createPassParameter.labelColor
        pass.foregroundColor = createPassParameter.foregroundColor
        pass.backgroundColor = createPassParameter.backgroundColor

        pass.organizationName = createPassParameter.organisationName
        pass.description = createPassParameter.description
        pass.logoText = createPassParameter.logoText

        pass.addBarcode("$baseUrl/showpass?serialNumber=${createPassParameter.passInfo.serialNumber}")

        return pass
    }

    private fun createHeaderFields(createPassParameter: CreatePassParameter): List<PKField> {
        val fields = mutableListOf<PKField>()

        if (createPassParameter.passInfo.issueStatus == IssueStatus.SIGNED)
            fields.add(PKField("testType", null, "TESTTYPE_${createPassParameter.passInfo.testType.name}"))
        else
            fields.add(PKField("issueStatus", null, "ISSUESTATUS_${createPassParameter.passInfo.issueStatus.name}"))

        return fields
    }

    private fun createPrimaryFields(createPassParameter: CreatePassParameter): List<PKField> {
        val fields = mutableListOf<PKField>()

        when (createPassParameter.passType) {
            PassType.COUPON -> fields.add(
                PKField("testResult", "TESTRESULT_${createPassParameter.passInfo.testResult.name}", "")
            )
            else -> fields.add(
                PKField("fullName", "FULLNAME", createPassParameter.passInfo.person.fullName)
            )
        }

        return fields
    }

    private fun createSecondaryFields(createPassParameter: CreatePassParameter): List<PKField> {
        val fields = mutableListOf<PKField>()

        if (createPassParameter.passType == PassType.COUPON)
            fields.add(PKField("fullName", "FULLNAME", createPassParameter.passInfo.person.fullName))
        else
            fields.add(
                PKField(
                    "testResult",
                    "TESTRESULT",
                    "TESTRESULT_${createPassParameter.passInfo.testResult.name}"
                )
            )

        val validUntilFormatted = createPassParameter.passInfo.validUntil?.format(dateTimeFormatter)
        if (validUntilFormatted != null && createPassParameter.passInfo.testResult != TestResultType.UNKNOWN) {
            val validUntilField = PKField("validUntil", "VALIDUNTIL", validUntilFormatted)
            validUntilField.dateStyle = PKDateStyle.PKDateStyleMedium
            validUntilField.timeStyle = PKDateStyle.PKDateStyleShort
            validUntilField.isRelative = true

            fields.add(validUntilField)
        }

        return fields
    }

    private fun createAuxiliaryFields(createPassParameter: CreatePassParameter): List<PKField> {
        val fields = mutableListOf<PKField>()

        fields.add(PKField("teststation", "TESTSTATION", createPassParameter.teststation.name))

        return fields
    }

    private fun createBackfields(createPassParameter: CreatePassParameter): List<PKField> {
        val validUntilFormatted = createPassParameter.passInfo.validUntil?.format(dateTimeFormatter)

        val fields = mutableListOf<PKField>()
        if (!createPassParameter.passInfo.person.email.isNullOrEmpty())
            fields.add(PKField("email", "EMAIL", createPassParameter.passInfo.person.email))

        if (!createPassParameter.passInfo.person.phoneNo.isNullOrEmpty())
            fields.add(PKField("phoneNo", "PHONENO", createPassParameter.passInfo.person.phoneNo))

        fields.add(PKField("teststationId", "TESTSTATIONID", createPassParameter.passInfo.teststationId))
        fields.add(PKField("testerId", "TESTERID", createPassParameter.passInfo.testerId))

        if (validUntilFormatted != null) {
            val validUntilField1 = PKField("validUntil1", "VALIDUNTIL", validUntilFormatted)
            validUntilField1.dateStyle = PKDateStyle.PKDateStyleMedium
            validUntilField1.timeStyle = PKDateStyle.PKDateStyleShort
            fields.add(validUntilField1)
        }

        fields.add(
            PKField(
                "showPassUrl",
                "SHOWPASSURL",
                "${baseUrl}/showpass?serialNumber=${createPassParameter.passInfo.serialNumber}"
            )
        )

        fields.add(PKField("terms", "TERMSCONDITIONS", "TERMSCONDITIONS_VALUE"))

        return fields
    }

    fun buildSignedPassPayload(
        keyName: String,
        privateKeyPassword: String,
        passImage: PassImage,
        passType: PassType,
        pass: PKPass
    ): ByteArray? {
        val appleWWDRCA = "certs/AppleWWDRCA.cer"
        val privateKeyPath = "certs/$keyName.p12"
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
