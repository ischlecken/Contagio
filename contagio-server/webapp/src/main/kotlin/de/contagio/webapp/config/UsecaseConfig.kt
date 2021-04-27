package de.contagio.webapp.config

import de.contagio.core.domain.port.*
import de.contagio.core.usecase.ListOfAllTesterInfo
import de.contagio.core.usecase.SearchPassInfo
import de.contagio.core.usecase.SearchTesterWithTeststation
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.TesterRepository
import de.contagio.webapp.repository.mongodb.TeststationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UsecaseConfig(
    private val passInfoRepository: PassInfoRepository,
    private val testerRepository: TesterRepository,
    private val teststationRepository: TeststationRepository
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
    open fun listOfAllTesterInfo(
        findAllTester: IFindAllTester,
        findAllTeststation: IFindAllTeststation
    ) = ListOfAllTesterInfo(findAllTeststation, findAllTester)

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
    open fun searchTestWithTeststation(
        findTester: IFindTester,
        findTeststation: IFindTeststation
    ) = SearchTesterWithTeststation(findTester, findTeststation)

    @Bean
    open fun searchPassInfo(
        findPassInfo: IFindPassInfo,
        searchTesterWithTeststation: SearchTesterWithTeststation
    ) = SearchPassInfo(findPassInfo, searchTesterWithTeststation)
}
