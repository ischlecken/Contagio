package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.usecase.CreatePass
import de.contagio.webapp.model.properties.ContagioProperties
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import javax.crypto.Cipher

private var logger = LoggerFactory.getLogger(PassBuilder::class.java)

@Service
class PassBuilder(
    private val contagioProperties: ContagioProperties
) {


    fun buildPkPass(
        passInfo: PassInfo,
        teststation: Teststation,
        passImage: PassImage,
        passType: PassType = PassType.GENERIC,
        labelColor: String = contagioProperties.pass.labelColor,
        foregroundColor: String = contagioProperties.pass.foregroundColor,
        backgroundColor: String = contagioProperties.pass.backgroundColor
    ): ByteArray? {
        val createPass = CreatePass(
            teamIdentifier = contagioProperties.pass.teamIdentifier,
            passTypeIdentifier = contagioProperties.pass.passTypeId,
            authenticationToken = passInfo.authToken,
            baseUrl = contagioProperties.baseUrl
        )

        val createPassParameter = CreatePassParameter(
            passInfo = passInfo,
            teststation = teststation,
            organisationName = contagioProperties.pass.organisationName,
            description = contagioProperties.pass.description,
            logoText = contagioProperties.pass.logoText,
            passType = passType,
            labelColor = labelColor,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor
        )

        return createPass.buildSignedPassPayload(
            contagioProperties.pass.keyName,
            contagioProperties.pass.privateKeyPassword,
            passImage,
            passType,
            createPass.build(createPassParameter)
        )
    }

    fun sign(pass: Pass): ByteArray? {
        var result: ByteArray? = null
        val keyStoreFile = PassBuilder::class.java.getResourceAsStream(contagioProperties.sign.keystore)

        if (keyStoreFile != null) {
            try {

                val keyStore = KeyStore.getInstance("PKCS12")
                val keyStorePassword = contagioProperties.sign.password.toCharArray()
                keyStore.load(keyStoreFile, keyStorePassword)
                val privateKey = keyStore.getKey(contagioProperties.sign.keyname, keyStorePassword) as PrivateKey
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
