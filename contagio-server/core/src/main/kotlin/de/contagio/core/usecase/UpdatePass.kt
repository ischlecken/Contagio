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
        validUntil: Instant?,
        passSigningInfo: PassSigningInfo
    ): UpdatePassResponse? {
        var result: UpdatePassResponse? = null

        getEncryptionKey.execute(serialNumber)?.let { authToken ->
            findPassInfoEnvelope.execute(serialNumber)?.let { passInfoEnvelope ->

                searchTesterWithTeststation.execute(passInfoEnvelope.testerId)?.let { testerTeststation ->

                    findEncryptedPayload.execute(passInfoEnvelope.passInfoId)?.let { encryptedPassInfo ->

                        (encryptedPassInfo.getObject(authToken, PassInfo::class.java) as? PassInfo)?.let { passInfo ->
                            findEncryptedPayload.execute(passInfo.imageId)?.let { passImageEncrypted ->

                                passImageEncrypted.get(authToken)?.let { passImage ->
                                    val updatedPassInfoEnvelope = if (validUntil != null)
                                        passInfoEnvelope.copy(
                                            validUntil = validUntil
                                        )
                                    else
                                        passInfoEnvelope

                                    val updatedPassInfo = if (testResult != null)
                                        passInfo.copy(testResult = testResult)
                                    else
                                        passInfo

                                    val passBuilderInfo = PassBuilderInfo(
                                        passSigningInfo = passSigningInfo,
                                        passImage = passImage,
                                        passInfoEnvelope = updatedPassInfoEnvelope,
                                        passInfo = updatedPassInfo,
                                        teststation = testerTeststation.teststation,
                                        tester = testerTeststation.tester
                                    )

                                    val pbr = PassBuilder(passBuilderInfo, urlBuilder)
                                        .build(authToken)
                                        ?.apply {

                                            saveRawEncryptedPayload.execute(
                                                updatedPassInfo.passId,
                                                this.pass,
                                                authToken
                                            )
                                            saveEncryptedPayload.execute(
                                                updatedPassInfoEnvelope.passInfoId,
                                                updatedPassInfo,
                                                authToken
                                            )
                                            savePassInfoEnvelope.execute(updatedPassInfoEnvelope)
                                        }

                                    if (pbr != null)
                                        result = UpdatePassResponse(
                                            authToken = authToken,
                                            passInfoEnvelope = passInfoEnvelope,
                                            passInfo = passInfo,
                                            pkPass = pbr.pkpass,
                                            pass = pbr.pass
                                        )
                                }
                            }
                        }
                    }
                }
            }
        }

        return result
    }

}
