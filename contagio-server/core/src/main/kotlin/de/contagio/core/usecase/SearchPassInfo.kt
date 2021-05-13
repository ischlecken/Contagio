package de.contagio.core.usecase

import de.contagio.core.domain.entity.ExtendedPassInfo
import de.contagio.core.domain.port.IFindPassInfoEnvelope

class SearchPassInfo(
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val searchTesterWithTeststation: SearchTesterWithTeststation
) {

    fun execute(id: String): ExtendedPassInfo? {
        var result: ExtendedPassInfo? = null

        findPassInfoEnvelope.execute(id)?.let { passInfoEnvelope ->
            searchTesterWithTeststation.execute(passInfoEnvelope.testerId)?.let {
                result = ExtendedPassInfo(
                    passInfoEnvelope = passInfoEnvelope,
                    testerTeststation = it
                )
            }
        }

        return result
    }
}
