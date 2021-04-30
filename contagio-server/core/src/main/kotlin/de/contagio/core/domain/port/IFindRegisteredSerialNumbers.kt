package de.contagio.core.domain.port


fun interface IFindRegisteredSerialNumbers {
    fun execute(deviceLibraryIdentifier: String): Collection<String>
}
