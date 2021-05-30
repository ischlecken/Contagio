package de.contagio.core.usecase

import de.contagio.core.domain.entity.EncryptedPayload
import de.contagio.core.domain.entity.ExtendedPassInfo
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.port.IFindEncryptedPayload
import de.contagio.core.domain.port.IFindPassInfoEnvelope
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.IdType

class SearchPassInfo(
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val findEncryptedPayload: IFindEncryptedPayload,
    private val getEncryptionKey: IGetEncryptionKey,
    private val searchTesterWithTeststation: SearchTesterWithTeststation
) {

    fun execute(serialNumber: String): ExtendedPassInfo? {
        val passInfoEnvelope = findPassInfoEnvelope.execute(serialNumber)
        val testerTeststation = searchTesterWithTeststation.execute(passInfoEnvelope?.testerId)
        val encryptedPassInfo = findEncryptedPayload.execute(passInfoEnvelope?.passInfoId)
        val key = getEncryptionKey.execute(IdType.SERIALNUMBER, serialNumber)
        val passInfo = encryptedPassInfo?.getObject(key, PassInfo::class.java) as? PassInfo
        val encryptedPass = findEncryptedPayload.execute(passInfoEnvelope?.passId) as? EncryptedPayload
        val pass = encryptedPass?.get(key)

        return if (passInfoEnvelope != null && testerTeststation != null && encryptedPassInfo != null)
            ExtendedPassInfo(
                passInfoEnvelope = passInfoEnvelope,
                testerTeststation = testerTeststation,
                passInfo = passInfo,
                pass = pass,
                passUpdated = encryptedPass?.updated
            )
        else
            null
    }
}
