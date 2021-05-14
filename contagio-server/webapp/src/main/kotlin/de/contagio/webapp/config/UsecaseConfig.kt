package de.contagio.webapp.config

import de.contagio.core.domain.port.*
import de.contagio.core.usecase.*
import de.contagio.webapp.model.properties.ContagioProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UsecaseConfig {

    @Bean
    open fun listOfAllTesterInfo(
        findAllTester: IFindAllTester,
        findAllTeststation: IFindAllTeststation
    ) = ListOfAllTesterInfo(findAllTeststation, findAllTester)

    @Bean
    open fun searchAllTesterTeststation(
        findAllTester: IFindAllTester,
        findAllTeststation: IFindAllTeststation
    ) = SearchAllTesterTeststation(findAllTeststation, findAllTester)

    @Bean
    open fun searchTestWithTeststation(
        findTester: IFindTester,
        findTeststation: IFindTeststation
    ) = SearchTesterWithTeststation(findTester, findTeststation)

    @Bean
    open fun searchTeststation(
        findTeststation: IFindTeststation
    ) = SearchTeststation(findTeststation)

    @Bean
    open fun createTeststation(
        saveTeststation: ISaveTeststation
    ) = CreateTeststation(saveTeststation)

    @Bean
    open fun updateTeststation(
        saveTeststation: ISaveTeststation
    ) = UpdateTeststation(saveTeststation)


    @Bean
    open fun createTester(
        saveTester: ISaveTester
    ) = CreateTester(saveTester)

    @Bean
    open fun updateTester(
        saveTester: ISaveTester
    ) = UpdateTester(saveTester)


    @Bean
    open fun searchPassInfo(
        findPassInfoEnvelope: IFindPassInfoEnvelope,
        findEncryptedPayload: IFindEncryptedPayload,
        getEncryptionKey: IGetEncryptionKey,
        searchTesterWithTeststation: SearchTesterWithTeststation
    ) = SearchPassInfo(findPassInfoEnvelope, findEncryptedPayload, getEncryptionKey, searchTesterWithTeststation)

    @Bean
    open fun searchPassesForDevice(
        findRegisteredSerialNumbers: IFindRegisteredSerialNumbers,
        findPassInfoEnvelope: IFindPassInfoEnvelope
    ) = SearchPassesForDevice(findRegisteredSerialNumbers, findPassInfoEnvelope)

    @Bean
    open fun urlBuilder(contagioProperties: ContagioProperties): UrlBuilder {
        return UrlBuilder(contagioProperties.baseUrl)
    }
}
