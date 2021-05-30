package de.contagio.core.usecase

import de.contagio.core.domain.entity.ExtendedPassInfo
import de.contagio.core.domain.port.IDeleteUpdatePassRequest
import de.contagio.core.domain.port.IFindUpdatePassRequest
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.IdType

class LazyUpdatePassInfo(
    private val getEncryptionKey: IGetEncryptionKey,
    private val findUpdatePassRequest: IFindUpdatePassRequest,
    private val deleteUpdatePassRequest: IDeleteUpdatePassRequest,
    private val updatePass: UpdatePass,
    private val searchPassInfo: SearchPassInfo
) {

    fun execute(serialNumber: String): ExtendedPassInfo? {
        val authToken = getEncryptionKey.execute(IdType.SERIALNUMBER, serialNumber) ?: return null

        findUpdatePassRequest.execute(serialNumber)?.let { upr ->
            updatePass.execute(
                authToken = authToken,
                serialNumber = serialNumber,
                testResult = upr.testResult,
                issueStatus = upr.issueStatus,
                validUntil = upr.validUntil,
                updated = upr.created
            )

            deleteUpdatePassRequest.execute(serialNumber)
        }

        return searchPassInfo.execute(serialNumber)
    }
}
