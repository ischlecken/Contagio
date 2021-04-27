package de.contagio.core.usecase

import de.contagio.core.domain.entity.ExtendedPassInfo
import de.contagio.core.domain.port.IFindPassInfo

class SearchPassInfo(
    private val findPassInfo: IFindPassInfo,
    private val searchTesterWithTeststation: SearchTesterWithTeststation
) {

    fun execute(id: String): ExtendedPassInfo? {
        var result: ExtendedPassInfo? = null

        findPassInfo.execute(id)?.let { passInfo ->
            searchTesterWithTeststation.execute(passInfo.testerId)?.let {
                result = ExtendedPassInfo(
                    passInfo = passInfo,
                    testerTeststation = it
                )
            }
        }

        return result
    }
}
