package de.contagio.core.usecase

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

const val encryptAlgorithm = "AES/CBC/PKCS5Padding"

class Encryptor {

    fun generateKey(): SecretKey {
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")

        keyGenerator.init(256)

        return keyGenerator.generateKey()
    }

    fun generateIV(): IvParameterSpec {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        return IvParameterSpec(iv)
    }

    fun execute(input: ByteArray, key: SecretKey, iv: IvParameterSpec): ByteArray {
        val cipher: Cipher = Cipher.getInstance(encryptAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)

        return cipher.doFinal(input)
    }
}
