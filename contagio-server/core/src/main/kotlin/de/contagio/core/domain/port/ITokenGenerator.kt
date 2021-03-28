package de.contagio.core.domain.port

fun interface ITokenGenerator {
    fun generate(): String
}
