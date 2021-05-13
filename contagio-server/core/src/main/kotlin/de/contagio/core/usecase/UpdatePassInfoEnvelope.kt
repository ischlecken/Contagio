package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.IFindEncryptedPayload
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.ISaveEncryptedPayload
import de.contagio.core.domain.port.ISavePassInfoEnvelope
import java.time.Instant
import java.time.temporal.ChronoUnit

class UpdatePassInfoEnvelope(
    private val findEncryptedPayload: IFindEncryptedPayload,
    private val savePassInfoEnvelope: ISavePassInfoEnvelope,
    private val saveEncryptedPayload: ISaveEncryptedPayload,
    private val getEncryptionKey: IGetEncryptionKey
) {

    fun execute(
        passInfoEnvelope: PassInfoEnvelope,
        testResult: TestResultType,
        issueStatus: IssueStatus,
        validUntil: Instant? = null
    ): PassInfoEnvelope? {

        return getEncryptionKey.execute(passInfoEnvelope.serialNumber)?.let { authToken ->
            findEncryptedPayload.execute(passInfoEnvelope.passInfoId)?.let { encryptedPayload ->
                val passInfo = encryptedPayload.getObject(authToken, PassInfo::class.java) as PassInfo

                var newValidUntil: Instant?
                val updatedPassInfo = passInfo.let {
                    newValidUntil = when {
                        issueStatus == IssueStatus.EXPIRED -> passInfoEnvelope.validUntil
                        validUntil != null -> validUntil
                        else -> Instant.now()
                            .plus(if (it.testType == TestType.VACCINATION) 24 * 364 else 1, ChronoUnit.DAYS)
                    }

                    it.copy(
                        testResult = testResult,
                    )
                }

                saveEncryptedPayload.execute(passInfoEnvelope.passInfoId, updatedPassInfo, authToken)

                val updatedPassInfoEnvelope = passInfoEnvelope.copy(
                    issueStatus = issueStatus,
                    updated = Instant.now(),
                    validUntil = newValidUntil,
                    version = passInfoEnvelope.version + 1
                )

                savePassInfoEnvelope.execute(updatedPassInfoEnvelope)

                updatedPassInfoEnvelope
            }
        }
    }
}
