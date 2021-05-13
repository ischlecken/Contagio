package de.contagio.core.usecase

import de.contagio.core.domain.port.IFindPassInfoEnvelope
import de.contagio.core.domain.port.IFindRegisteredSerialNumbers
import java.time.Instant

data class PassSerialNumberWithUpdated(val serialNumber: String, val updated: Instant)

class SearchPassesForDevice(
    private val findRegisteredSerialNumbers: IFindRegisteredSerialNumbers,
    private val findPassInfoEnvelope: IFindPassInfoEnvelope
) {
    fun execute(deviceLibraryIdentifier: String): Collection<PassSerialNumberWithUpdated> {
        val result = mutableListOf<PassSerialNumberWithUpdated>()

        findRegisteredSerialNumbers.execute(deviceLibraryIdentifier).forEach {
            findPassInfoEnvelope
                .execute(it)
                ?.let { passInfo ->
                    result.add(PassSerialNumberWithUpdated(passInfo.serialNumber, passInfo.updated))
                }
        }

        return result
    }
}
