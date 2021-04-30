package de.contagio.webapp.config

import de.contagio.core.domain.port.*
import de.contagio.webapp.repository.mongodb.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
open class PortConfig(
    private val passInfoRepository: PassInfoRepository,
    private val passRepository: PassRepository,
    private val testerRepository: TesterRepository,
    private val teststationRepository: TeststationRepository,
    private val registrationInfoRepository: RegistrationInfoRepository
) {

    @Bean
    open fun findAllTester(): IFindAllTester {
        return IFindAllTester {
            testerRepository.findAll()
        }
    }

    @Bean
    open fun findAllTeststation(): IFindAllTeststation {
        return IFindAllTeststation {
            teststationRepository.findAll()
        }
    }

    @Bean
    open fun findPassInfo(): IFindPassInfo {
        return IFindPassInfo { id ->
            val result = passInfoRepository.findById(id)

            return@IFindPassInfo if (result.isPresent)
                result.get()
            else
                null
        }
    }

    @Bean
    open fun findPass(): IFindPass {
        return IFindPass { id ->
            val passInfo = passInfoRepository.findById(id)
            val pass = passInfo
                .flatMap {
                    if (!it.passId.isNullOrEmpty())
                        passRepository.findById(it.passId!!)
                    else
                        Optional.empty()
                }

            return@IFindPass if (pass.isPresent)
                PassInfoPass(passInfo = passInfo.get(), pass = pass.get())
            else
                null
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
    open fun findRegistrations(): IFindRegistrations {
        return IFindRegistrations { deviceLibraryIdentifier, serialNumber ->
            registrationInfoRepository.findByDeviceLibraryIdentifierAndSerialNumber(
                deviceLibraryIdentifier,
                serialNumber
            )
        }
    }
}
