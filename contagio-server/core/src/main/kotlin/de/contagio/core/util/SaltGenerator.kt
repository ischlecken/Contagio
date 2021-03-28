package de.contagio.core.util

import de.contagio.core.domain.port.ISaltGenerator
import java.util.*

class SaltGenerator : ISaltGenerator {
    override fun generate(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}
