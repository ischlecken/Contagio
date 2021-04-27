package de.contagio.webapp.config

import de.contagio.core.domain.port.IFindAllTester
import de.contagio.core.domain.port.IFindAllTeststation
import de.contagio.webapp.repository.mongodb.TesterRepository
import de.contagio.webapp.repository.mongodb.TeststationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UsecaseConfig(
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
}
