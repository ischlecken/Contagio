package de.contagio.webapp.config

import de.contagio.core.domain.entity.EncryptedPayload
import de.contagio.core.domain.port.*
import de.contagio.webapp.repository.mongodb.*
import de.contagio.webapp.service.AuthTokenService
import de.contagio.webapp.util.toPageRequest
import de.contagio.webapp.util.toPagedResult
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
            val result = encryptedPayloadRepository.findById(id)

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
            registrationInfoRepository.findByDeviceLibraryIdentifierAndSerialNumber(
                deviceLibraryIdentifier,
                serialNumber
            )
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
    open fun getEncryptionKey(): IGetEncryptionKey {
        return IGetEncryptionKey { id ->
            authTokenService.getAuthToken(id)
        }
    }

}
