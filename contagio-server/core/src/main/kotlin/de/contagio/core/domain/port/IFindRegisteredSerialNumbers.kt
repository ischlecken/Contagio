package de.contagio.core.domain.port

import de.contagio.core.usecase.PassSerialNumberWithUpdated


fun interface IFindRegisteredSerialNumbers {
    fun execute(deviceLibraryIdentifier: String): Collection<String>
}


fun interface IFindPendingSerialNumbers {
    fun execute(): Collection<PassSerialNumberWithUpdated>
}
