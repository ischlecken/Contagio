package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.usecase.PassBuilder
import de.contagio.webapp.model.properties.ContagioProperties
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import javax.crypto.Cipher

private var logger = LoggerFactory.getLogger(PassBuilder::class.java)

@Service
class PassBuilderService(
    private val contagioProperties: ContagioProperties
) {
    @Value("classpath:certs/pass.p12")
    private lateinit var passKeystore: Resource

    @Value("classpath:certs/AppleWWDRCA.cer")
    private lateinit var appleWWDRCA: Resource

    @Value("classpath:certs/contagio-sign.p12")
    private lateinit var contagioSignKeystore: Resource

    fun buildPkPass(
        passImage: PassImage,
        passInfo: PassInfo,
        teststation: Teststation,
        tester: Tester,
    ): ByteArray? {

        val passCoreInfo = PassCoreInfo(
            teamIdentifier = contagioProperties.pass.teamIdentifier,
            passTypeIdentifier = contagioProperties.pass.passTypeId,
            authenticationToken = passInfo.authToken,
            baseUrl = contagioProperties.baseUrl,
            organisationName = contagioProperties.pass.organisationName,
        )

        val passSigningInfo = PassSigningInfo(
            keystore = passKeystore.inputStream,
            keystorePassword = contagioProperties.pass.keystorePassword,
            appleWWDRCA = appleWWDRCA.inputStream
        )

        val passBuilderInfo = PassBuilderInfo(
            passCoreInfo = passCoreInfo,
            passSigningInfo = passSigningInfo,
            passImage = passImage,
            passInfo = passInfo,
            teststation = teststation,
            tester = tester
        )

        val passBuilderResult = PassBuilder(passBuilderInfo).build()

        return passBuilderResult.pass
    }

    fun sign(pass: Pass): ByteArray? {
        var result: ByteArray? = null
        val keyStoreFile = contagioSignKeystore.inputStream

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

        return result
    }
}
