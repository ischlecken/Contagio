package de.contagio.core.usecase

import de.contagio.core.domain.port.IFindPassInfoEnvelope
import de.contagio.core.domain.port.IFindPendingSerialNumbers
import de.contagio.core.domain.port.IFindRegisteredSerialNumbers
import org.slf4j.LoggerFactory
import java.time.Instant

private var logger = LoggerFactory.getLogger(SearchPassesForDevice::class.java)

data class PassSerialNumberWithUpdated(val serialNumber: String, val updated: Instant)

class SearchPassesForDevice(
    private val findRegisteredSerialNumbers: IFindRegisteredSerialNumbers,
    private val findPendingSerialNumbers: IFindPendingSerialNumbers,
    private val findPassInfoEnvelope: IFindPassInfoEnvelope
) {
    fun execute(deviceLibraryIdentifier: String, updatedSince: Instant?): Collection<PassSerialNumberWithUpdated> {
        val serialNumbers = mutableListOf<PassSerialNumberWithUpdated>()

        findRegisteredSerialNumbers.execute(deviceLibraryIdentifier).forEach {
            findPassInfoEnvelope.execute(it)?.let { passInfoEnvelope ->
                serialNumbers.add(
                    PassSerialNumberWithUpdated(
                        passInfoEnvelope.serialNumber,
                        passInfoEnvelope.updated
                    )
                )
            }
        }

        serialNumbers.addAll(findPendingSerialNumbers.execute())

        return if (updatedSince != null) {
            logger.debug("updatedSince=$updatedSince")

            serialNumbers.filter {
                logger.debug("  updated=${it.updated}")

                it.updated.isAfter(updatedSince)
            }
        } else
            serialNumbers
    }
}
