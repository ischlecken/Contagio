package de.contagio.core.usecase

import de.brendamour.jpasskit.PKField
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.enums.PKDataDetectorType
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private val logger = LoggerFactory.getLogger(PassBuilder::class.java)
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'+00:00'")

class PassBuilder(
    private val passBuilderInfo: PassBuilderInfo,
    private val urlBuilder: UrlBuilder
) {

    fun build(): PassBuilderResult {
        var result: ByteArray? = null
        val pkpass = buildPKPass()

        try {

            if (pkpass.isValid) {
                val cpt = ContagioPassTemplate(
                    passBuilderInfo.passInfoEnvelope.authToken,
                    passBuilderInfo.passImage,
                    passBuilderInfo.passInfoEnvelope.passType,
                    passBuilderInfo.passInfoEnvelope.issueStatus
                )
                cpt.build()

                val pkSigningInformation =
                    PKSigningInformationUtil().loadSigningInformationFromPKCS12AndIntermediateCertificate(
                        passBuilderInfo.passSigningInfo.keystore,
                        passBuilderInfo.passSigningInfo.keystorePassword,
                        passBuilderInfo.passSigningInfo.appleWWDRCA
                    )

                result = PKFileBasedSigningUtil().createSignedAndZippedPkPassArchive(pkpass, cpt, pkSigningInformation)
            } else {
                logger.debug("pass is NOT valid: ${pkpass.validationErrors}")
            }
        } catch (e: Exception) {
            logger.error("Error while creating signed pass payload", e)

            throw e
        }

        return PassBuilderResult(pkpass = pkpass, pass = result)
    }

    private fun buildPKPass(): PKPass {
        val pass = initCorePass()

        val generic = when (passBuilderInfo.passInfoEnvelope.passType) {
            PassType.GENERIC -> PKGenericPass()
            PassType.COUPON -> PKCoupon()
            PassType.EVENT -> PKEventTicket()
            PassType.STORE -> PKStoreCard()
        }

        generic.headerFields = createHeaderFields()
        generic.primaryFields = createPrimaryFields()
        generic.secondaryFields = createSecondaryFields()
        generic.auxiliaryFields = createAuxiliaryFields()
        generic.backFields = createBackfields()

        when (passBuilderInfo.passInfoEnvelope.passType) {
            PassType.GENERIC -> pass.generic = generic
            PassType.COUPON -> pass.coupon = generic as PKCoupon?
            PassType.EVENT -> pass.eventTicket = generic as PKEventTicket?
            PassType.STORE -> pass.storeCard = generic as PKStoreCard?
        }

        return pass
    }

    private fun initCorePass(): PKPass {
        val pass = PKPass()

        with(passBuilderInfo.passCoreInfo) {
            pass.passTypeIdentifier = this.passTypeIdentifier
            pass.teamIdentifier = this.teamIdentifier
            pass.webServiceURL = URL(urlBuilder.walletURL)
            pass.authenticationToken = this.authenticationToken
            pass.organizationName = this.organisationName
            pass.isSharingProhibited = true
        }
        with(passBuilderInfo.passInfoEnvelope) {
            pass.serialNumber = this.serialNumber

            if (this.issueStatus == IssueStatus.REVOKED || this.issueStatus == IssueStatus.EXPIRED)
                pass.expirationDate = Date.from(Instant.now())
            else if (this.validUntil != null)
                pass.expirationDate = Date.from(this.validUntil)

            pass.labelColor = this.labelColor
            pass.foregroundColor = this.foregroundColor
            pass.backgroundColor = this.backgroundColor
            pass.description = this.description
            pass.logoText = this.logoText

            pass.addBarcode(urlBuilder.verifyURL(this.serialNumber))
        }

        return pass
    }

    private fun createHeaderFields(): List<PKField> {
        val fields = mutableListOf<PKField>()

        fields.add(PKField("testType", null, "TESTTYPE_${passBuilderInfo.passInfoEnvelope.testType.name}"))

        return fields
    }

    private fun createPrimaryFields(): List<PKField> {
        val fields = mutableListOf<PKField>()

        when (passBuilderInfo.passInfoEnvelope.passType) {
            PassType.COUPON -> fields.add(
                if (passBuilderInfo.passInfoEnvelope.issueStatus == IssueStatus.REVOKED || passBuilderInfo.passInfoEnvelope.issueStatus == IssueStatus.EXPIRED)
                    PKField("issueStatus", "ISSUESTATUS_${passBuilderInfo.passInfoEnvelope.issueStatus}", "")
                else
                    PKField("testResult", "TESTRESULT", "TESTRESULT_${passBuilderInfo.passInfoEnvelope.testResult.name}")
            )
            else -> fields.add(
                PKField("fullName", "FULLNAME", passBuilderInfo.passInfoEnvelope.person.fullName)
            )
        }

        return fields
    }

    private fun createSecondaryFields(): List<PKField> {
        val fields = mutableListOf<PKField>()

        if (passBuilderInfo.passInfoEnvelope.passType == PassType.COUPON)
            fields.add(PKField("fullName", "FULLNAME", passBuilderInfo.passInfoEnvelope.person.fullName))
        else
            fields.add(
                PKField(
                    "testResult",
                    "TESTRESULT",
                    "TESTRESULT_${passBuilderInfo.passInfoEnvelope.testResult.name}"
                )
            )

        val validUntilFormatted =
            passBuilderInfo.passInfoEnvelope.validUntil?.atZone(ZoneId.of("UTC"))?.format(dateTimeFormatter)
        if (validUntilFormatted != null &&
            passBuilderInfo.passInfoEnvelope.testResult != TestResultType.UNKNOWN &&
            passBuilderInfo.passInfoEnvelope.issueStatus == IssueStatus.ISSUED
        ) {
            val validUntilField = PKField("validUntil", "VALIDUNTIL", validUntilFormatted)
            validUntilField.dateStyle = PKDateStyle.PKDateStyleMedium
            validUntilField.timeStyle = PKDateStyle.PKDateStyleShort
            validUntilField.isRelative = true
            validUntilField.changeMessage = "Neues Ablaufdatum ist %@"

            fields.add(validUntilField)
        }

        return fields
    }

    private fun createAuxiliaryFields(): List<PKField> {
        val fields = mutableListOf<PKField>()

        fields.add(PKField("teststation", "TESTSTATION", passBuilderInfo.teststation.name))

        return fields
    }

    private fun createBackfields(): List<PKField> {
        return with(passBuilderInfo.passInfoEnvelope) {
            val fields = mutableListOf<PKField>()

            val validUntilFormatted = this.validUntil?.atZone(ZoneId.of("UTC"))?.format(dateTimeFormatter)

            val showPassUrl = PKField(
                "showPassUrl",
                "SHOWPASSURL",
                urlBuilder.verifyURL(this.serialNumber)
            )
            showPassUrl.dataDetectorTypes = listOf(PKDataDetectorType.PKDataDetectorTypeLink)
            fields.add(showPassUrl)

            fields.add(PKField("testtype", "TESTTYPE", "TESTTYPE_${this.testType}"))
            fields.add(PKField("testresult", "TESTRESULT", "TESTRESULT_${this.testResult}"))
            if (validUntilFormatted != null) {
                val validUntilField1 = PKField("validUntil1", "VALIDUNTIL", validUntilFormatted)
                validUntilField1.dateStyle = PKDateStyle.PKDateStyleMedium
                validUntilField1.timeStyle = PKDateStyle.PKDateStyleShort
                fields.add(validUntilField1)
            }

            fields.add(PKField("teststationName", "TESTSTATIONNAME", passBuilderInfo.teststation.name))
            fields.add(PKField("testerName", "TESTERNAME", passBuilderInfo.tester.person.fullName))

            if (!this.person.email.isNullOrEmpty())
                fields.add(PKField("email", "EMAIL", this.person.email))

            if (!this.person.phoneNo.isNullOrEmpty()) {
                val emailField = PKField("phoneNo", "PHONENO", this.person.phoneNo)
                emailField.dataDetectorTypes = listOf(PKDataDetectorType.PKDataDetectorTypePhoneNumber)
                fields.add(emailField)
            }


            fields.add(PKField("terms", "TERMSCONDITIONS", "TERMSCONDITIONS_VALUE"))

            fields
        }
    }

}
