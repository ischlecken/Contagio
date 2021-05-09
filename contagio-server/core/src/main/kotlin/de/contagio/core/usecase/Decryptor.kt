package de.contagio.core.usecase

import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class Decryptor {

    fun execute(input: ByteArray, keyBase64: String, ivBase64: String): ByteArray {
        val key = SecretKeySpec(Base64.getDecoder().decode(keyBase64), algorithm)
        val iv = IvParameterSpec(Base64.getDecoder().decode(ivBase64))

        return execute(input, key, iv)
    }

    fun execute(input: ByteArray, key: SecretKey, iv: IvParameterSpec): ByteArray {
        val cipher: Cipher = Cipher.getInstance(encryptAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)

        return cipher.doFinal(input)
    }
}
