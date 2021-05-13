package de.contagio.core.usecase

import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import javax.crypto.Cipher


private var logger = LoggerFactory.getLogger(SignatureBuilder::class.java)

class SignatureBuilder(
    private val keyStoreFile: InputStream,
    private val keyStorePassword: String,
    private val keyName:String
) {

    fun execute(data: ByteArray): ByteArray? {
        var result: ByteArray? = null

        try {
            val keyStore = KeyStore.getInstance("PKCS12")
            val keyStorePassword = keyStorePassword.toCharArray()
            keyStore.load(keyStoreFile, keyStorePassword)
            val privateKey = keyStore.getKey(keyName, keyStorePassword) as PrivateKey
            logger.debug("sign(): privateKey=${Hex.encodeHexString(privateKey.encoded)}")

            val md: MessageDigest = MessageDigest.getInstance("SHA-256")
            val messageHash: ByteArray = md.digest(data)
            logger.debug("sign(): messageHash=${Hex.encodeHexString(messageHash)}")

            val cipher: Cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.ENCRYPT_MODE, privateKey)
            val digitalSignature: ByteArray = cipher.doFinal(messageHash)

            logger.debug("sign(): signature=${Hex.encodeHexString(digitalSignature)}")

            result = digitalSignature
        } catch (ex: Exception) {
            logger.error("Exception while signing data", ex)
        }

        return result
    }
}
