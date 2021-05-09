package de.contagio.core.usecase

import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

const val encryptAlgorithm = "AES/CBC/PKCS5Padding"
const val algorithm = "AES"

class Encryptor {

    fun generateKey(): SecretKey {
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance(algorithm)

        keyGenerator.init(256)

        return keyGenerator.generateKey()
    }

    fun generateIV(): IvParameterSpec {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        return IvParameterSpec(iv)
    }

    fun execute(input: ByteArray, keyBase64: String, ivBase64: String): ByteArray {
        val key = SecretKeySpec(Base64.getDecoder().decode(keyBase64), algorithm)
        val iv = IvParameterSpec(Base64.getDecoder().decode(ivBase64))

        return execute(input, key, iv)
    }

    fun execute(input: ByteArray, key: SecretKey, iv: IvParameterSpec): ByteArray {
        val cipher: Cipher = Cipher.getInstance(encryptAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)

        return cipher.doFinal(input)
    }
}
