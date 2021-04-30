package de.contagio.core.domain.port

import de.contagio.core.domain.entity.RegistrationInfo

fun interface IFindRegistrations {
    fun execute(deviceLibraryIdentifier: String, serialNumber: String): Collection<RegistrationInfo>
}
