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

private val logger = LoggerFactory.getLogger(PassBuilder::class.java)

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'+02:00'")

class PassBuilder(private val passBuilderInfo: PassBuilderInfo) {


    fun build(): PassBuilderResult {
        var result: ByteArray? = null
        val pkpass = buildPKPass()

        try {

            if (pkpass.isValid) {
                val cpt = ContagioPassTemplate(passBuilderInfo.passImage, passBuilderInfo.passInfo.passType)
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
        }

        return PassBuilderResult(pkpass = pkpass, pass = result)
    }

    private fun buildPKPass(): PKPass {
        val pass = initCorePass()

        val generic = when (passBuilderInfo.passInfo.passType) {
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

        when (passBuilderInfo.passInfo.passType) {
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
            pass.webServiceURL = URL("$baseUrl/co_v1/wallet/")
            pass.authenticationToken = this.authenticationToken
            pass.organizationName = this.organisationName
            pass.isSharingProhibited = true
        }
        with(passBuilderInfo.passInfo) {
            pass.serialNumber = this.serialNumber

            if (this.validUntil != null)
                pass.expirationDate =
                    Date.from(this.validUntil.atZone(ZoneId.systemDefault()).toInstant())

            pass.labelColor = this.labelColor
            pass.foregroundColor = this.foregroundColor
            pass.backgroundColor = this.backgroundColor
            pass.description = this.description
            pass.logoText = this.logoText

            pass.addBarcode("${passBuilderInfo.passCoreInfo.baseUrl}/showpass?serialNumber=${this.serialNumber}")
        }

        return pass
    }

    private fun createHeaderFields(): List<PKField> {
        val fields = mutableListOf<PKField>()

        if (passBuilderInfo.passInfo.issueStatus == IssueStatus.ISSUED)
            fields.add(PKField("testType", null, "TESTTYPE_${passBuilderInfo.passInfo.testType.name}"))
        else
            fields.add(PKField("issueStatus", null, "ISSUESTATUS_${passBuilderInfo.passInfo.issueStatus.name}"))

        return fields
    }

    private fun createPrimaryFields(): List<PKField> {
        val fields = mutableListOf<PKField>()

        when (passBuilderInfo.passInfo.passType) {
            PassType.COUPON -> fields.add(
                PKField("testResult", "TESTRESULT", "TESTRESULT_${passBuilderInfo.passInfo.testResult.name}")
            )
            else -> fields.add(
                PKField("fullName", "FULLNAME", passBuilderInfo.passInfo.person.fullName)
            )
        }

        return fields
    }

    private fun createSecondaryFields(): List<PKField> {
        val fields = mutableListOf<PKField>()

        if (passBuilderInfo.passInfo.passType == PassType.COUPON)
            fields.add(PKField("fullName", "FULLNAME", passBuilderInfo.passInfo.person.fullName))
        else
            fields.add(
                PKField(
                    "testResult",
                    "TESTRESULT",
                    "TESTRESULT_${passBuilderInfo.passInfo.testResult.name}"
                )
            )

        val validUntilFormatted = passBuilderInfo.passInfo.validUntil?.format(dateTimeFormatter)
        if (validUntilFormatted != null && passBuilderInfo.passInfo.testResult != TestResultType.UNKNOWN) {
            val validUntilField = PKField("validUntil", "VALIDUNTIL", validUntilFormatted)
            validUntilField.dateStyle = PKDateStyle.PKDateStyleMedium
            validUntilField.timeStyle = PKDateStyle.PKDateStyleShort
            validUntilField.isRelative = true

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
        return with(passBuilderInfo.passInfo) {
            val fields = mutableListOf<PKField>()

            val validUntilFormatted = this.validUntil?.format(dateTimeFormatter)

            if (!this.person.email.isNullOrEmpty())
                fields.add(PKField("email", "EMAIL", this.person.email))

            if (!this.person.phoneNo.isNullOrEmpty())
                fields.add(PKField("phoneNo", "PHONENO", this.person.phoneNo))

            fields.add(PKField("teststationId", "TESTSTATIONID", this.teststationId))
            fields.add(PKField("testerName", "TESTERNAME", passBuilderInfo.tester.person.fullName))

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
                    "${passBuilderInfo.passCoreInfo.baseUrl}/showpass?serialNumber=${this.serialNumber}"
                )
            )

            fields.add(PKField("terms", "TERMSCONDITIONS", "TERMSCONDITIONS_VALUE"))

            fields
        }
    }

}
