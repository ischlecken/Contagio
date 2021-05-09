package de.contagio.core.util

import de.contagio.core.domain.port.IAuthTokenGenerator
import de.contagio.core.usecase.Encryptor
import java.util.*

class AuthTokenGenerator : IAuthTokenGenerator {
    override fun generate(): String {
        val key = Encryptor().generateKey()

        return Base64.getEncoder().encodeToString(key.encoded)
    }
}
