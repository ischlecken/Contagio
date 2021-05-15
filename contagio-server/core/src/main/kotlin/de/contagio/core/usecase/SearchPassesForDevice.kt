package de.contagio.core.usecase

import de.contagio.core.domain.entity.EncryptedPayload
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.port.*
import java.time.Instant

data class PassSerialNumberWithUpdated(val serialNumber: String, val updated: Instant)

class SearchPassesForDevice(
    private val findRegisteredSerialNumbers: IFindRegisteredSerialNumbers,
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val findEncryptedPayload: IFindEncryptedPayload,
    private val getEncryptionKey: IGetEncryptionKey
) {
    fun execute(deviceLibraryIdentifier: String): Collection<PassSerialNumberWithUpdated> {
        val result = mutableListOf<PassSerialNumberWithUpdated>()

        findRegisteredSerialNumbers.execute(deviceLibraryIdentifier).forEach {
            findPassInfoEnvelope.execute(it)?.let { passInfoEnvelope ->
                findEncryptedPayload.execute(passInfoEnvelope.passInfoId)?.let { encryptedPassInfo ->
                    val key = getEncryptionKey.execute(IdType.SERIALNUMBER, passInfoEnvelope.serialNumber)
                    val passInfo = encryptedPassInfo.getObject(key, PassInfo::class.java) as? PassInfo
                    val encryptedPass = findEncryptedPayload.execute(passInfo?.passId) as? EncryptedPayload

                    if (encryptedPass != null)
                        result.add(PassSerialNumberWithUpdated(passInfoEnvelope.serialNumber, encryptedPass.updated))
                }
            }
        }

        return result
    }
}
