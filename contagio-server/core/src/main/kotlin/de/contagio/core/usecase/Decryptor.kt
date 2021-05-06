package de.contagio.core.usecase

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class Decryptor {

    fun execute(input: ByteArray, key: SecretKey, iv: IvParameterSpec):ByteArray {
        val cipher: Cipher = Cipher.getInstance(encryptAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)

        return cipher.doFinal(input)
    }
}
