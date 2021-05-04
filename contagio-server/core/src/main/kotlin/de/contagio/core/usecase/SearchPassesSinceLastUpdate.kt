package de.contagio.core.usecase

import de.contagio.core.domain.port.IFindPassInfo
import de.contagio.core.domain.port.IFindRegisteredSerialNumbers
import java.time.Instant

data class PassSerialNumberWithUpdated(val serialNumber: String, val updated: Instant)

class SearchPassesSinceLastUpdate(
    private val findRegisteredSerialNumbers: IFindRegisteredSerialNumbers,
    private val findPassInfo: IFindPassInfo
) {
    fun execute(
        deviceLibraryIdentifier: String,
        updatedSince: Instant? = null
    ): Collection<PassSerialNumberWithUpdated> {
        val result = mutableListOf<PassSerialNumberWithUpdated>()

        findRegisteredSerialNumbers.execute(deviceLibraryIdentifier).forEach {
            findPassInfo.execute(it)?.let { passInfo ->
                result.add(PassSerialNumberWithUpdated(passInfo.serialNumber, passInfo.updated))
            }
        }

        return result
    }
}