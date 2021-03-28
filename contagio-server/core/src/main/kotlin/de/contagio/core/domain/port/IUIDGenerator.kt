package de.contagio.core.domain.port

fun interface IUIDGenerator {
    fun generate(): String
}
