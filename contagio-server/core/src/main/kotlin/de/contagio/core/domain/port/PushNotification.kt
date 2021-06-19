package de.contagio.core.domain.port

import de.contagio.core.domain.entity.DeviceToken

fun interface IFindDeviceTokensForSerialnumber {
    fun execute(serialNumber: String): Collection<String>
}


fun interface IFindDeviceToken {
    fun execute(deviceToken: String): Collection<DeviceToken>
}

fun interface ISaveDeviceToken {
    fun execute(deviceToken: DeviceToken)
}

fun interface IDeleteDeviceToken {
    fun execute(deviceToken: String)
}
