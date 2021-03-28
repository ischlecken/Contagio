package de.contagio.core.util

import de.contagio.core.domain.port.ITokenGenerator
import java.util.*

class TokenGenerator : ITokenGenerator {
    override fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
