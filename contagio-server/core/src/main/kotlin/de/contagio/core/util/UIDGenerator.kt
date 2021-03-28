package de.contagio.core.util

import de.contagio.core.domain.port.IUIDGenerator
import java.util.*

class UIDGenerator : IUIDGenerator {
    override fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
