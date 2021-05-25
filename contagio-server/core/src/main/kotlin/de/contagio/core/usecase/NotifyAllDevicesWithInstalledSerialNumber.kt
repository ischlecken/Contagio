package de.contagio.core.usecase

import de.contagio.core.domain.entity.PassUpdateLog
import de.contagio.core.domain.port.IFindDeviceInfo
import de.contagio.core.domain.port.IFindRegistrationInfoBySerialNumber
import de.contagio.core.domain.port.INotifyDevice
import de.contagio.core.domain.port.ISavePassUpdateLog

class NotifyAllDevicesWithInstalledSerialNumber(
    private val findRegistrationInfoBySerialNumber: IFindRegistrationInfoBySerialNumber,
    private val findDeviceInfo: IFindDeviceInfo,
    private val notifyDevice: INotifyDevice,
    private val savePassUpdateLog: ISavePassUpdateLog
) {
    fun execute(id: String): Boolean {
        var notified = false

        findRegistrationInfoBySerialNumber.execute(id).forEach { registrationInfo ->
            findDeviceInfo.execute(registrationInfo.deviceLibraryIdentifier)?.let { deviceInfo ->
                notifyDevice.execute(id, deviceInfo)
                notified = true
            }
        }

        savePassUpdateLog.execute(
            PassUpdateLog(
                id,
                "NotifyAllDevicesWithInstalledSerialNumber",
                if (notified) "Device notified" else "no device found"
            )
        )

        return notified
    }
}
