package de.contagio.core.usecase

import de.contagio.core.domain.entity.PassInfoEnvelope
import de.contagio.core.domain.port.IFindPassInfoEnvelope
import de.contagio.core.domain.port.ISavePassInfoEnvelope

class UpdatePassInfoEnvelope(
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val savePassInfoEnvelope: ISavePassInfoEnvelope
) {

    fun execute(
        id: String,
        update: (PassInfoEnvelope) -> PassInfoEnvelope
    ): PassInfoEnvelope? {

        return findPassInfoEnvelope.execute(id)?.let { passInfoEnvelope ->
            val updatedPassInfoEnvelope = update(passInfoEnvelope)

            savePassInfoEnvelope.execute(updatedPassInfoEnvelope)

            updatedPassInfoEnvelope
        }
    }
}
