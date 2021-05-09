package de.contagio.core.domain.port

fun interface ITokenGenerator {
    fun generate(): String
}


fun interface ISaltGenerator {
    fun generate(): String
}

fun interface IUIDGenerator {
    fun generate(): String
}

fun interface IAuthTokenGenerator {
    fun generate(): String
}
