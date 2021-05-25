package de.contagio.core.usecase

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.port.IDeleteEncryptedPayload
import de.contagio.core.domain.port.IFindEncryptedPayload
import de.contagio.core.domain.port.IFindPassInfoEnvelope

class DeletePass(
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val findEncryptedPayload: IFindEncryptedPayload,
    private val deleteEncryptedPayload: IDeleteEncryptedPayload,
    private val updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope,
) {

    fun execute(authToken: String, serialNumber: String) {
        val passInfoEnvelope = findPassInfoEnvelope.execute(serialNumber)
        val encryptedPassInfo = findEncryptedPayload.execute(passInfoEnvelope?.passInfoId)
        val passInfo = encryptedPassInfo?.getObject(authToken, PassInfo::class.java) as? PassInfo

        deleteEncryptedPayload.execute(passInfo?.imageId)
        deleteEncryptedPayload.execute(passInfo?.passId)
        deleteEncryptedPayload.execute(passInfoEnvelope?.passInfoId)

        updateOnlyPassInfoEnvelope.execute(serialNumber) {
            it.copy(issueStatus = IssueStatus.DELETED)
        }
    }

}
