package de.contagio.core.usecase

import de.contagio.core.domain.entity.DeviceToken
import de.contagio.core.domain.port.IDeleteDeviceToken
import de.contagio.core.domain.port.ISaveDeviceToken

class RegisterDeviceToken(
    private val saveDeviceToken: ISaveDeviceToken,
    private val deleteDeviceToken: IDeleteDeviceToken
) {

    fun execute(
        deviceToken: String,
        bundleId: String,
        serialNumbers: Collection<String>
    ) {
        deleteDeviceToken.execute(deviceToken)

        serialNumbers.forEach {
            saveDeviceToken.execute(DeviceToken(deviceToken, bundleId, it))
        }
    }
}
