package de.contagio.core.usecase

import de.brendamour.jpasskit.PKBarcode
import de.brendamour.jpasskit.PKField
import de.brendamour.jpasskit.PKLocation
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.enums.PKBarcodeFormat
import de.brendamour.jpasskit.passes.PKGenericPass
import de.brendamour.jpasskit.signing.PKFileBasedSigningUtil
import de.brendamour.jpasskit.signing.PKSigningInformationUtil
import de.contagio.core.domain.entity.PassInfo
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

private val logger = LoggerFactory.getLogger(CreatePass::class.java)

class CreatePass(
    private val teamIdentifier: String,
    private val passTypeIdentifier: String,
    private val authenticationToken: String
) {

    fun build(passInfo: PassInfo): PKPass {
        val pass = PKPass()
        pass.passTypeIdentifier = this.passTypeIdentifier
        pass.authenticationToken = this.authenticationToken
        pass.serialNumber = passInfo.serialNumber
        pass.teamIdentifier = this.teamIdentifier

        pass.organizationName = "your org"
        pass.description = "some description"
        pass.logoText = "some logo text"

        val barcode = PKBarcode()
        barcode.format = PKBarcodeFormat.PKBarcodeFormatQR
        barcode.message = "123456789"
        barcode.messageEncoding = Charset.forName("iso-8859-1")

        pass.barcodes = listOf(barcode)

        val generic = PKGenericPass()
        val member = PKField()
        member.key = "mykey" // some unique key for primary field
        member.value = "myvalue" // some value

        generic.primaryFields = listOf(member)
        
        val member1 = PKField()
        member1.key = "UserId"
        member1.value = passInfo.userId
        generic.auxiliaryFields = listOf(member1)

        pass.generic = generic

        val location = PKLocation()
        location.latitude = 37.33182
        location.longitude = -122.03118

        pass.locations = listOf(location)

        return pass
    }

    fun buildSignedPassPayload(pass: PKPass): ByteArray? {
        val appleWWDRCA = "passbook/AppleWWDRCA.pem" // this is apple's developer relation cert
        val privateKeyPath = "./privateKey.p12" // the private key you exported from keychain
        val privateKeyPassword = "password" // the password you used to export
        var result: ByteArray? = null
        try {
            val pkSigningInformation =
                PKSigningInformationUtil().loadSigningInformationFromPKCS12AndIntermediateCertificate(
                    privateKeyPath,
                    privateKeyPassword,
                    appleWWDRCA
                )

            if (pass.isValid) {
                val pathToTemplateDirectory = "./mypass.raw" // replace with your folder with the icons

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
