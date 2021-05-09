package de.contagio.core.usecase

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

private val logger: Logger = LoggerFactory.getLogger(EncryptorTest::class.java)

class EncryptorTest {

    @Test
    fun generateKey_isNotNull() {
        val encryptor = Encryptor()
        val key = encryptor.generateKey()

        logger.debug("key={}", Base64.getEncoder().encodeToString(key.encoded))

        assertEquals(32, key.encoded.size)
    }

    @Test
    fun generateIV_isNotNull() {
        val encryptor = Encryptor()
        val iv = encryptor.generateIV()

        logger.debug("iv={}", Base64.getEncoder().encodeToString(iv.iv))

        assertEquals(16, iv.iv.size)
    }

    @Test
    fun encryptDecrypt_isEqual() {
        val encryptor = Encryptor()
        val key = encryptor.generateKey()
        val iv = encryptor.generateIV()

        logger.debug("key={}", Base64.getEncoder().encodeToString(key.encoded))
        logger.debug("iv={}", Base64.getEncoder().encodeToString(iv.iv))

        val message = "the quick brown fox jumps over the lazy dog"

        val encryptedMessage = encryptor.execute(message.toByteArray(), key, iv)

        logger.debug("encryptedMessage={}", Base64.getEncoder().encodeToString(encryptedMessage))

        val decryptor = Decryptor()
        val decryptedMessage = String(decryptor.execute(encryptedMessage, key, iv))

        logger.debug("decryptedMessage={}", decryptedMessage)

        assertEquals(message, decryptedMessage)
    }

    @Test
    fun encryptDecryptUsingBase64_isEqual() {
        val encryptor = Encryptor()
        val key =  Base64.getEncoder().encodeToString(encryptor.generateKey().encoded)
        val iv =  Base64.getEncoder().encodeToString(encryptor.generateIV().iv)

        logger.debug("key={}", key)
        logger.debug("iv={}", iv)

        val message = "the quick brown fox jumps over the lazy dog"
        val encryptedMessage = encryptor.execute(message.toByteArray(), key, iv)

        logger.debug("encryptedMessage={}", Base64.getEncoder().encodeToString(encryptedMessage))

        val decryptor = Decryptor()
        val decryptedMessage = String(decryptor.execute(encryptedMessage, key, iv))

        logger.debug("decryptedMessage={}", decryptedMessage)

        assertEquals(message, decryptedMessage)
    }
}
