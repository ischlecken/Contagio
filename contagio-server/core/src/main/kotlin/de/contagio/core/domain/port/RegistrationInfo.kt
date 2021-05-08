package de.contagio.core.domain.port

import de.contagio.core.domain.entity.DeviceInfo
import de.contagio.core.domain.entity.RegistrationInfo

fun interface IFindRegistrationInfo {
    fun execute(deviceLibraryIdentifier: String, serialNumber: String): Collection<RegistrationInfo>
}


fun interface IFindAllRegistrationInfo {
    fun execute(pageRequest: PageRequest): PagedResult<RegistrationInfo>
}


fun interface IFindAllDeviceInfo {
    fun execute(pageRequest: PageRequest): PagedResult<DeviceInfo>
}
