package de.contagio.core.usecase

import de.contagio.core.domain.port.IFindDeviceInfo
import de.contagio.core.domain.port.IFindRegistrationInfoBySerialNumber
import de.contagio.core.domain.port.INotifyDevice

class NotifyAllDevicesWithInstalledSerialNumber(
    private val findRegistrationInfoBySerialNumber: IFindRegistrationInfoBySerialNumber,
    private val findDeviceInfo: IFindDeviceInfo,
    private val notifyDevice: INotifyDevice
) {
    fun execute(id: String) {
        findRegistrationInfoBySerialNumber.execute(id).forEach { registrationInfo ->
            findDeviceInfo.execute(registrationInfo.deviceLibraryIdentifier)?.let { deviceInfo ->
                notifyDevice.execute(id, deviceInfo)
            }
        }
    }
}
