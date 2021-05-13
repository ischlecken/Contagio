package de.contagio.webapp.service

import de.contagio.core.domain.entity.Address
import de.contagio.core.domain.entity.Person
import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.entity.Teststation
import de.contagio.webapp.repository.mongodb.TesterRepository
import de.contagio.webapp.repository.mongodb.TeststationRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component


private var logger = LoggerFactory.getLogger(AppStartupRunner::class.java)

@Component
open class AppStartupRunner(
    private val teststationRepository: TeststationRepository,
    private val testerRepository: TesterRepository
) : ApplicationRunner {

    @Throws(Exception::class)
    override fun run(args: ApplicationArguments) {
        logger.info("AppStartupRunner.run()")

        val teststations = listOf(
            Teststation(
                id = "1",
                name = "Tübingen Marktplatz",
                address = Address(city = "Tübingen", zipcode = "12345", street = "Marktplatz", hno = "1")
            ),
            Teststation(
                id = "2",
                name = "München Riemarkaden",
                address = Address(city = "München", zipcode = "81673", "Willy-Brandt-Platz", hno = "10")
            )
        )

        val tester = listOf(
            Tester(
                id = "1",
                teststationId = "1",
                person = Person(firstName = "Emil", lastName = "Nolde")
            ),
            Tester(
                id = "2",
                teststationId = "1",
                person = Person(firstName = "Kunigunde", lastName = "Fischer")
            ),
            Tester(
                id = "3",
                teststationId = "1",
                person = Person(firstName = "Lisa", lastName = "Federle")
            ),
            Tester(
                id = "4",
                teststationId = "2",
                person = Person(firstName = "David", lastName = "Kuhn")
            ),
            Tester(
                id = "5",
                teststationId = "2",
                person = Person(firstName = "Manuel", lastName = "Langweiler")
            )
        )

        teststations.forEach {
            if (!teststationRepository.existsById(it.id))
                teststationRepository.save(it)
        }

        tester.forEach {
            if (!testerRepository.existsById(it.id))
                testerRepository.save(it)
        }
    }

}
