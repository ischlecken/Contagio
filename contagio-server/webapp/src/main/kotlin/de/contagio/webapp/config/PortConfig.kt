package de.contagio.webapp.config

import de.contagio.core.domain.entity.EncryptedPayload
import de.contagio.core.domain.entity.PassSigningInfo
import de.contagio.core.domain.port.*
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.*
import de.contagio.webapp.service.AuthTokenService
import de.contagio.webapp.service.PushNotificationService
import de.contagio.webapp.util.toPageRequest
import de.contagio.webapp.util.toPagedResult
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.util.*

private var logger = LoggerFactory.getLogger(PortConfig::class.java)

@Configuration
open class PortConfig(
    private val passInfoEnvelopeRepository: PassInfoEnvelopeRepository,
    private val encryptedPayloadRepository: EncryptedPayloadRepository,
    private val testerRepository: TesterRepository,
    private val teststationRepository: TeststationRepository,
    private val registrationInfoRepository: RegistrationInfoRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val deviceTokenRepository: DeviceTokenRepository,
    private val authTokenService: AuthTokenService,
) {

    @Bean
    open fun findPassInfoEnvelope() = IFindPassInfoEnvelope { serialNumber ->
        val result = if (serialNumber != null)
            passInfoEnvelopeRepository.findById(serialNumber)
        else
            Optional.empty()

        return@IFindPassInfoEnvelope if (result.isPresent)
            result.get()
        else
            null
    }


    @Bean
    open fun deletePassInfoEnvelope() = IDeletePassInfoEnvelope { id ->
        if (id != null)
            passInfoEnvelopeRepository.deleteById(id)
    }


    @Bean
    open fun findAllPassInfoEnvelope() = IFindAllPassInfoEnvelope {
        passInfoEnvelopeRepository.findAll(it.toPageRequest()).toPagedResult()
    }


    @Bean
    open fun savePassInfoEnvelope() = ISavePassInfoEnvelope {
        passInfoEnvelopeRepository.save(it)
    }

    @Bean
    open fun findEncryptedPayload() = IFindEncryptedPayload { id ->
        val result = if (id != null) encryptedPayloadRepository.findById(id) else Optional.empty()

        return@IFindEncryptedPayload if (result.isPresent)
            result.get()
        else
            null
    }

    @Bean
    open fun deleteEncryptedPayload() = IDeleteEncryptedPayload { id ->
        if (id != null)
            encryptedPayloadRepository.deleteById(id)
    }

    @Bean
    open fun saveEncryptedPayload() = ISaveEncryptedPayload { id, obj, key ->
        val result = EncryptedPayload.toEncryptedJsonPayload(id, obj, key)

        encryptedPayloadRepository.save(result)

        result
    }

    @Bean
    open fun saveRawEncryptedPayload() = ISaveRawEncryptedPayload { id, obj, key ->
        val result = EncryptedPayload.toEncryptedPayload(id, obj, key)

        encryptedPayloadRepository.save(result)

        result
    }

    @Bean
    open fun findAllTester() = IFindAllTester {
        testerRepository.findAll(it.toPageRequest()).toPagedResult()
    }

    @Bean
    open fun findTester() = IFindTester { id ->
        val result = testerRepository.findById(id)

        return@IFindTester if (result.isPresent)
            result.get()
        else
            null
    }

    @Bean
    open fun saveTester() = ISaveTester {
        testerRepository.save(it)
    }

    @Bean
    open fun deleteTester() = IDeleteTester {
        testerRepository.delete(it)
    }

    @Bean
    open fun findAllTeststation() = IFindAllTeststation {
        teststationRepository.findAll(it.toPageRequest()).toPagedResult()
    }

    @Bean
    open fun findTeststation() = IFindTeststation { id ->
        val result = teststationRepository.findById(id)

        return@IFindTeststation if (result.isPresent)
            result.get()
        else
            null
    }

    @Bean
    open fun saveTeststation() = ISaveTeststation {
        teststationRepository.save(it)
    }

    @Bean
    open fun deleteTeststation() = IDeleteTeststation {
        teststationRepository.delete(it)
    }

    @Bean
    open fun findRegisteredSerialNumbers() = IFindRegisteredSerialNumbers { deviceLibraryIdentifier ->
        registrationInfoRepository
            .findByDeviceLibraryIdentifier(deviceLibraryIdentifier)
            .map {
                it.serialNumber
            }
    }

    @Bean
    open fun findRegistrationInfo() = IFindRegistrationInfo { deviceLibraryIdentifier, serialNumber ->
        registrationInfoRepository
            .findByDeviceLibraryIdentifierAndSerialNumber(
                deviceLibraryIdentifier,
                serialNumber
            )
            .firstOrNull()
    }

    @Bean
    open fun findRegistrationInfoBySerialNumber() = IFindRegistrationInfoBySerialNumber { serialNumber ->
        registrationInfoRepository.findBySerialNumber(serialNumber)
    }

    @Bean
    open fun findDeviceTokenBySerialNumber() = IFindDeviceTokenBySerialNumber { serialNumber ->
        deviceTokenRepository.findBySerialNumber(serialNumber)
    }

    @Bean
    open fun findAllRegistrationInfo() = IFindAllRegistrationInfo {
        registrationInfoRepository.findAll(it.toPageRequest()).toPagedResult()
    }

    @Bean
    open fun findAllDeviceInfo() = IFindAllDeviceInfo {
        deviceInfoRepository.findAll(it.toPageRequest()).toPagedResult()
    }

    @Bean
    open fun findDeviceInfo() = IFindDeviceInfo {
        val result = deviceInfoRepository.findById(it)

        return@IFindDeviceInfo if (result.isPresent) result.get() else null
    }

    @Bean
    open fun getEncryptionKey() = IGetEncryptionKey { type, id ->
        authTokenService.getAuthToken(type, id)
    }

    @Bean
    open fun setEncryptionKey() = ISetEncryptionKey { type, id, key ->
        authTokenService.setAuthToken(type, id, key)
    }

    @Bean
    open fun notifyDevice(pushNotificationService: PushNotificationService) =
        INotifyDevice { serialNumber, deviceInfo ->

            logger.debug("notifyDevice() found push token ${deviceInfo.pushToken} for serialnumber $serialNumber...")

            pushNotificationService
                .send2WalletAsync(deviceInfo.pushToken)
                ?.thenApply {
                    logger.debug("  notifyDevice(): apns-id=${it.apnsId}")
                    logger.debug("  notifyDevice(): isAccepted=${it.isAccepted}")
                    logger.debug("  notifyDevice(): rejectionReason=${it.rejectionReason}")
                }
        }

    @Bean
    open fun notifyTeststation(pushNotificationService: PushNotificationService) =
        INotifyTeststation { serialNumber, deviceToken ->

            logger.debug("notifyTeststation(): found deviceToken ${deviceToken.deviceToken} for serialnumber $serialNumber...")

            pushNotificationService
                .send2TeststationAsync(serialNumber,deviceToken.deviceToken)
                ?.thenApply {
                    logger.debug("  notifyTeststation(): apns-id=${it.apnsId}")
                    logger.debug("  notifyTeststation(): isAccepted=${it.isAccepted}")
                    logger.debug("  notifyTeststation(): rejectionReason=${it.rejectionReason}")
                }
        }

    @Value("classpath:certs/pass.p12")
    private lateinit var passKeystore: Resource

    @Value("classpath:certs/AppleWWDRCA.cer")
    private lateinit var appleWWDRCA: Resource

    @Bean
    open fun passSigningInfo(contagioProperties: ContagioProperties) = PassSigningInfo(
        keystore = IOUtils.toByteArray(passKeystore.inputStream),
        keystorePassword = contagioProperties.pass.keystorePassword,
        appleWWDRCA = IOUtils.toByteArray(appleWWDRCA.inputStream)
    )


    @Bean
    open fun savePassUpdateLog(passUpdateLogRepository: PassUpdateLogRepository) = ISavePassUpdateLog {
        passUpdateLogRepository.save(it)
    }


    @Bean
    open fun findAllPassUpdateLog(passUpdateLogRepository: PassUpdateLogRepository) = IFindAllPassUpdateLog {
        passUpdateLogRepository.findAll(it.toPageRequest()).toPagedResult()
    }


    @Bean
    open fun findUpdatePassRequest(updatePassRequestRepository: UpdatePassRequestRepository) =
        IFindUpdatePassRequest { serialNumber ->
            val result = updatePassRequestRepository.findById(serialNumber)

            return@IFindUpdatePassRequest if (result.isPresent)
                result.get()
            else
                null
        }

    @Bean
    open fun saveUpdatePassRequest(updatePassRequestRepository: UpdatePassRequestRepository) = ISaveUpdatePassRequest {
        updatePassRequestRepository.save(it)
    }


    @Bean
    open fun deleteUpdatePassRequest(updatePassRequestRepository: UpdatePassRequestRepository) =
        IDeleteUpdatePassRequest {
            updatePassRequestRepository.deleteById(it)
        }


    @Bean
    open fun findDeviceToken(deviceTokenRepository: DeviceTokenRepository) = IFindDeviceToken {
        deviceTokenRepository.findByDeviceToken(it)
    }

    @Bean
    open fun saveDeviceToken(deviceTokenRepository: DeviceTokenRepository) = ISaveDeviceToken {
        deviceTokenRepository.save(it)
    }

    @Bean
    open fun deleteDeviceToken(deviceTokenRepository: DeviceTokenRepository) = IDeleteDeviceToken {
        val deviceTokens = deviceTokenRepository.findByDeviceToken(it)

        deviceTokenRepository.deleteAll(deviceTokens)
    }

    @Bean
    open fun findDeviceTokensForSerialnumber(deviceTokenRepository: DeviceTokenRepository) =
        IFindDeviceTokensForSerialnumber {
            deviceTokenRepository.findBySerialNumber(it).map { it.deviceToken }
        }
}
