package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.usecase.CreatePass
import de.contagio.core.usecase.CreatePassParameter
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassImageRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.time.LocalDateTime
import javax.crypto.Cipher

private var logger = LoggerFactory.getLogger(PassBuilder::class.java)

@Service
class PassBuilder(
    private val passInfoRepository: PassInfoRepository,
    private val passImageRepository: PassImageRepository,
    private val passRepository: PassRepository,
    private val contagioProperties: ContagioProperties
) {

    private val uidGenerator = UIDGenerator()

    fun build(
        image: MultipartFile,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String?,
        teststationId: String,
        testerId: String,
        testResult: TestResultType,
        testType: TestType
    ): PassInfo? {

        val passInfo = PassInfo.build(
            uidGenerator,
            firstName,
            lastName,
            phoneNo,
            email,
            teststationId,
            testerId,
            testResult,
            testType
        )

        val passImage = PassImage.build(image.bytes, image.contentType, passInfo)

        return build(passInfo, passImage)
    }


    fun build(passInfo: PassInfo, passImage: PassImage): PassInfo? {
        val passInfo1 = passInfo.copy(
            validUntil = LocalDateTime.now().plusHours(12)
        )

        val pkpass = buildPkPass(passInfo1, passImage)

        return if (pkpass != null) {
            val passInfo2 = passInfo1.copy(
                issueStatus = IssueStatus.SIGNED,
                passId = uidGenerator.generate()
            )

            passRepository.save(Pass(id = passInfo2.passId!!, data = pkpass))
            passImageRepository.save(passImage)
            passInfoRepository.save(passInfo2)
        } else {
            passImageRepository.save(passImage)
            passInfoRepository.save(passInfo.copy(issueStatus = IssueStatus.REFUSED))
        }
    }

    fun buildPkPass(
        passInfo: PassInfo,
        passImage: PassImage,
        passType: PassType = PassType.GENERIC,
        templateName: String? = null
    ): ByteArray? {
        val createPass = CreatePass(
            teamIdentifier = contagioProperties.teamIdentifier,
            passTypeIdentifier = contagioProperties.passTypeId,
            authenticationToken = passInfo.authToken,
            baseUrl = contagioProperties.baseUrl
        )

        val createPassParameter = CreatePassParameter(
            organisationName = contagioProperties.passOrganisationName,
            description = contagioProperties.passDescription,
            logoText = contagioProperties.passLogoText,
            passType = passType
        )

        return createPass.buildSignedPassPayload(
            contagioProperties.passResourcesDir,
            contagioProperties.keyName,
            contagioProperties.privateKeyPassword,
            templateName ?: contagioProperties.templateName,
            passImage,
            createPass.build(passInfo, createPassParameter)
        )
    }

    fun sign(pass: Pass): ByteArray? {
        var result: ByteArray? = null
        val keyStoreFile = PassBuilder::class.java.getResourceAsStream("/contagio-sign.p12")

        if (keyStoreFile != null) {
            try {

                val keyStore = KeyStore.getInstance("PKCS12")
                val keyStorePassword = "contagio".toCharArray()
                keyStore.load(keyStoreFile, keyStorePassword)
                val privateKey = keyStore.getKey("contagiosign", keyStorePassword) as PrivateKey
                logger.debug("sign(): privateKey=${Hex.encodeHexString(privateKey.encoded)}")

                val md: MessageDigest = MessageDigest.getInstance("SHA-256")
                val messageHash: ByteArray = md.digest(pass.data)
                logger.debug("sign(): messageHash=${Hex.encodeHexString(messageHash)}")

                val cipher: Cipher = Cipher.getInstance("RSA")
                cipher.init(Cipher.ENCRYPT_MODE, privateKey)
                val digitalSignature: ByteArray = cipher.doFinal(messageHash)

                logger.debug("sign(): signature=${Hex.encodeHexString(digitalSignature)}")

                result = digitalSignature
            } catch (ex: Exception) {
                logger.error("Exception while signing pass", ex)
            }
        }

        return result
    }
}
