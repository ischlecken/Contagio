package de.contagio.core.domain.port


fun interface IGetEncryptionKey {
    fun execute(id: String): String?
}
