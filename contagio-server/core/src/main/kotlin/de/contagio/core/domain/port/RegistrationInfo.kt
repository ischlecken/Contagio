package de.contagio.core.domain.port

import de.contagio.core.domain.entity.DeviceInfo
import de.contagio.core.domain.entity.RegistrationInfo

fun interface IFindRegistrationInfo {
    fun execute(deviceLibraryIdentifier: String, serialNumber: String): RegistrationInfo?
}

fun interface IFindRegistrationInfoBySerialNumber {
    fun execute(serialNumber: String): Collection<RegistrationInfo>
}


fun interface IFindAllRegistrationInfo {
    fun execute(pageRequest: PageRequest): PagedResult<RegistrationInfo>
}


fun interface IFindAllDeviceInfo {
    fun execute(pageRequest: PageRequest): PagedResult<DeviceInfo>
}


fun interface IFindDeviceInfo {
    fun execute(deviceLibraryIdentifier: String): DeviceInfo?
}

fun interface INotifyDevice {
    fun execute(serialNumber:String, deviceInfo: DeviceInfo)
}
