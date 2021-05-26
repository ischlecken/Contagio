package de.contagio.core.usecase

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.port.IDeleteEncryptedPayload
import de.contagio.core.domain.port.IFindPassInfoEnvelope

class DeletePass(
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val deleteEncryptedPayload: IDeleteEncryptedPayload,
    private val updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope,
) {

    fun execute(serialNumber: String) {
        val passInfoEnvelope = findPassInfoEnvelope.execute(serialNumber)

        deleteEncryptedPayload.execute(passInfoEnvelope?.imageId)
        deleteEncryptedPayload.execute(passInfoEnvelope?.passId)
        deleteEncryptedPayload.execute(passInfoEnvelope?.passInfoId)

        updateOnlyPassInfoEnvelope.execute(serialNumber) {
            it.copy(issueStatus = IssueStatus.DELETED)
        }
    }

}
