package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.*
import java.time.Instant

class UpdatePass(
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val findEncryptedPayload: IFindEncryptedPayload,
    private val savePassInfoEnvelope: ISavePassInfoEnvelope,
    private val saveEncryptedPayload: ISaveEncryptedPayload,
    private val saveRawEncryptedPayload: ISaveRawEncryptedPayload,
    private val searchTesterWithTeststation: SearchTesterWithTeststation,
    private val getEncryptionKey: IGetEncryptionKey,
    private val urlBuilder: UrlBuilder
) {

    fun execute(
        serialNumber: String,
        testResult: TestResultType?,
        issueStatus: IssueStatus?,
        validUntil: Instant?,
        passSigningInfo: PassSigningInfo
    ): UpdatePassResponse? {

        val authToken = getEncryptionKey.execute(serialNumber)
        val passInfoEnvelope = findPassInfoEnvelope.execute(serialNumber)
        val testerTeststation = searchTesterWithTeststation.execute(passInfoEnvelope?.testerId)
        val encryptedPassInfo = findEncryptedPayload.execute(passInfoEnvelope?.passInfoId)
        val passInfo = encryptedPassInfo?.getObject(authToken, PassInfo::class.java) as? PassInfo
        val passImageEncrypted = findEncryptedPayload.execute(passInfo?.imageId)
        val passImage = passImageEncrypted?.get(authToken)

        var updatedPassInfoEnvelope = passInfoEnvelope
        if (issueStatus != null)
            updatedPassInfoEnvelope = updatedPassInfoEnvelope?.copy(issueStatus = issueStatus)
        if (validUntil != null)
            updatedPassInfoEnvelope = updatedPassInfoEnvelope?.copy(validUntil = validUntil)

        var updatedPassInfo = passInfo
        if (testResult != null)
            updatedPassInfo = updatedPassInfo?.copy(testResult = testResult)

        return if (
            authToken != null &&
            passImage != null &&
            updatedPassInfo != null &&
            updatedPassInfoEnvelope != null &&
            testerTeststation != null
        ) {
            val passBuilderInfo = PassBuilderInfo(
                passSigningInfo = passSigningInfo,
                passImage = passImage,
                passInfoEnvelope = updatedPassInfoEnvelope,
                passInfo = updatedPassInfo,
                teststation = testerTeststation.teststation,
                tester = testerTeststation.tester
            )

            PassBuilder(passBuilderInfo, urlBuilder)
                .build(authToken)
                ?.let {
                    saveRawEncryptedPayload.execute(updatedPassInfo.passId, it.pass, authToken)
                    saveEncryptedPayload.execute(updatedPassInfoEnvelope.passInfoId, updatedPassInfo, authToken)
                    savePassInfoEnvelope.execute(updatedPassInfoEnvelope)

                    UpdatePassResponse(
                        authToken = authToken,
                        passInfoEnvelope = updatedPassInfoEnvelope,
                        passInfo = updatedPassInfo,
                        pkPass = it.pkpass,
                        pass = it.pass
                    )
                }
        } else
            null
    }

}
