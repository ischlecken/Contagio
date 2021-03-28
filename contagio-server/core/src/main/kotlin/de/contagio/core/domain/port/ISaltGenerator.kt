package de.contagio.core.domain.port

fun interface ISaltGenerator {
    fun generate(): String
}
