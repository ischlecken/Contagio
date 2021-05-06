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
        findTeststation: IFindTeststation,
        saveTeststation: ISaveTeststation
    ) = UpdateTeststation(findTeststation, saveTeststation)


    @Bean
    open fun searchPassInfo(
        findPassInfo: IFindPassInfo,
        searchTesterWithTeststation: SearchTesterWithTeststation
    ) = SearchPassInfo(findPassInfo, searchTesterWithTeststation)

    @Bean
    open fun searchPassesSinceLastUpdate(
        findRegisteredSerialNumbers: IFindRegisteredSerialNumbers,
        findPassInfo: IFindPassInfo
    ) = SearchPassesSinceLastUpdate(findRegisteredSerialNumbers, findPassInfo)

    @Bean
    open fun urlBuilder(contagioProperties: ContagioProperties): UrlBuilder {
        return UrlBuilder(contagioProperties.baseUrl)
    }
}
