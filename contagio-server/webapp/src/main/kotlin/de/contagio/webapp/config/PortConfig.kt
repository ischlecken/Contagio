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
    private val authTokenService: AuthTokenService,
) {

    @Bean
    open fun findPassInfoEnvelope(): IFindPassInfoEnvelope {
        return IFindPassInfoEnvelope { id ->
            val result = passInfoEnvelopeRepository.findById(id)

            return@IFindPassInfoEnvelope if (result.isPresent)
                result.get()
            else
                null
        }
    }

    @Bean
    open fun deletePassInfoEnvelope(): IDeletePassInfoEnvelope {
        return IDeletePassInfoEnvelope { id ->
            val result = passInfoEnvelopeRepository.findById(id)

            if (result.isPresent)
                passInfoEnvelopeRepository.deleteById(id)

            return@IDeletePassInfoEnvelope if (result.isPresent)
                result.get()
            else
                null
        }
    }

    @Bean
    open fun findAllPassInfoEnvelope(): IFindAllPassInfoEnvelope {
        return IFindAllPassInfoEnvelope {
            passInfoEnvelopeRepository.findAll(it.toPageRequest()).toPagedResult()
        }
    }

    @Bean
    open fun savePassInfoEnvelope(): ISavePassInfoEnvelope {
        return ISavePassInfoEnvelope {
            passInfoEnvelopeRepository.save(it)
        }
    }


    @Bean
    open fun findEncryptedPayload(): IFindEncryptedPayload {
        return IFindEncryptedPayload { id ->
            val result = if (id != null) encryptedPayloadRepository.findById(id) else Optional.empty()

            return@IFindEncryptedPayload if (result.isPresent)
                result.get()
            else
                null
        }
    }

    @Bean
    open fun saveEncryptedPayload(): ISaveEncryptedPayload {
        return ISaveEncryptedPayload { id, obj, key ->
            val result = EncryptedPayload.toEncryptedJsonPayload(id, obj, key)

            encryptedPayloadRepository.save(result)

            result
        }
    }

    @Bean
    open fun saveRawEncryptedPayload(): ISaveRawEncryptedPayload {
        return ISaveRawEncryptedPayload { id, obj, key ->
            val result = EncryptedPayload.toEncryptedPayload(id, obj, key)

            encryptedPayloadRepository.save(result)

            result
        }
    }

    @Bean
    open fun findAllTester(): IFindAllTester {
        return IFindAllTester {
            testerRepository.findAll(it.toPageRequest()).toPagedResult()
        }
    }


    @Bean
    open fun findTester(): IFindTester {
        return IFindTester { id ->
            val result = testerRepository.findById(id)

            return@IFindTester if (result.isPresent)
                result.get()
            else
                null
        }
    }


    @Bean
    open fun saveTester(): ISaveTester {
        return ISaveTester {
            testerRepository.save(it)
        }
    }

    @Bean
    open fun deleteTester(): IDeleteTester {
        return IDeleteTester {
            testerRepository.delete(it)
        }
    }


    @Bean
    open fun findAllTeststation(): IFindAllTeststation {
        return IFindAllTeststation {
            teststationRepository.findAll(it.toPageRequest()).toPagedResult()
        }
    }

    @Bean
    open fun findTeststation(): IFindTeststation {
        return IFindTeststation { id ->
            val result = teststationRepository.findById(id)

            return@IFindTeststation if (result.isPresent)
                result.get()
            else
                null
        }
    }

    @Bean
    open fun saveTeststation(): ISaveTeststation {
        return ISaveTeststation {
            teststationRepository.save(it)
        }
    }

    @Bean
    open fun deleteTeststation(): IDeleteTeststation {
        return IDeleteTeststation {
            teststationRepository.delete(it)
        }
    }

    @Bean
    open fun findRegisteredSerialNumbers(): IFindRegisteredSerialNumbers {
        return IFindRegisteredSerialNumbers { deviceLibraryIdentifier ->

            registrationInfoRepository
                .findByDeviceLibraryIdentifier(deviceLibraryIdentifier)
                .map {
                    it.serialNumber
                }
        }
    }

    @Bean
    open fun findRegistrationInfo(): IFindRegistrationInfo {
        return IFindRegistrationInfo { deviceLibraryIdentifier, serialNumber ->
            registrationInfoRepository
                .findByDeviceLibraryIdentifierAndSerialNumber(
                    deviceLibraryIdentifier,
                    serialNumber
                )
                .firstOrNull()
        }
    }

    @Bean
    open fun findRegistrationInfoBySerialNumber(): IFindRegistrationInfoBySerialNumber {
        return IFindRegistrationInfoBySerialNumber { serialNumber ->
            registrationInfoRepository.findBySerialNumber(serialNumber)
        }
    }


    @Bean
    open fun findAllRegistrationInfo(): IFindAllRegistrationInfo {
        return IFindAllRegistrationInfo {
            registrationInfoRepository.findAll(it.toPageRequest()).toPagedResult()
        }
    }

    @Bean
    open fun findAllDeviceInfo(): IFindAllDeviceInfo {
        return IFindAllDeviceInfo {
            deviceInfoRepository.findAll(it.toPageRequest()).toPagedResult()
        }
    }

    @Bean
    open fun findDeviceInfo(): IFindDeviceInfo {
        return IFindDeviceInfo {
            val result = deviceInfoRepository.findById(it)

            return@IFindDeviceInfo if (result.isPresent) result.get() else null
        }
    }


    @Bean
    open fun getEncryptionKey(): IGetEncryptionKey {
        return IGetEncryptionKey { type, id ->
            authTokenService.getAuthToken(type, id)
        }
    }


    @Bean
    open fun setEncryptionKey(): ISetEncryptionKey {
        return ISetEncryptionKey { type, id, key ->
            authTokenService.setAuthToken(type, id, key)
        }
    }

    @Bean
    open fun notifyDevice(pushNotificationService: PushNotificationService): INotifyDevice {
        return INotifyDevice { serialNumber, deviceInfo ->

            logger.debug("found push token ${deviceInfo.pushToken} for serialnumber $serialNumber...")

            pushNotificationService
                .sendPushNotificationAsync(deviceInfo.pushToken)
                ?.thenApply {
                    logger.debug("  apns-id=${it.apnsId}")
                    logger.debug("  isAccepted=${it.isAccepted}")
                    logger.debug("  rejectionReason=${it.rejectionReason}")
                }
        }
    }


    @Value("classpath:certs/pass.p12")
    private lateinit var passKeystore: Resource

    @Value("classpath:certs/AppleWWDRCA.cer")
    private lateinit var appleWWDRCA: Resource

    @Bean
    open fun passSigningInfo(contagioProperties:ContagioProperties) = PassSigningInfo(
        keystore = IOUtils.toByteArray(passKeystore.inputStream),
        keystorePassword = contagioProperties.pass.keystorePassword,
        appleWWDRCA = IOUtils.toByteArray(appleWWDRCA.inputStream)
    )
}
