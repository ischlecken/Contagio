package de.contagio.core.usecase

import de.contagio.core.domain.entity.PassSerialNumberWithUpdated
import de.contagio.core.domain.port.IFindPassInfoEnvelope
import de.contagio.core.domain.port.IFindRegisteredSerialNumbers
import de.contagio.core.domain.port.IFindUpdatePassRequest
import de.contagio.core.lastModifiedDateTime
import org.slf4j.LoggerFactory
import java.time.Instant

private var logger = LoggerFactory.getLogger(SearchPassesForDevice::class.java)

class SearchPassesForDevice(
    private val findRegisteredSerialNumbers: IFindRegisteredSerialNumbers,
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val findUpdatePassRequest: IFindUpdatePassRequest
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

        val result = mutableListOf<PassSerialNumberWithUpdated>()
        serialNumbers.forEach { s ->
            result.add(
                findUpdatePassRequest.execute(s.serialNumber)?.let {
                    PassSerialNumberWithUpdated(serialNumber = s.serialNumber, updated = it.created)
                } ?: s
            )
        }

        return if (updatedSince != null) {
            logger.debug("  updatedSince=${updatedSince.lastModifiedDateTime()}")

            result.filter { s ->
                logger.debug("    updated=${s.updated.lastModifiedDateTime()}")

                s.updated.isAfter(updatedSince)
            }
        } else
            serialNumbers
    }
}
