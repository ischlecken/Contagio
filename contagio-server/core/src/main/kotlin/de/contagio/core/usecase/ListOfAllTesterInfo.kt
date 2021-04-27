package de.contagio.core.usecase

import de.contagio.core.domain.entity.TesterInfo
import de.contagio.core.domain.entity.TesterTeststation
import de.contagio.core.domain.port.IFindAllTester
import de.contagio.core.domain.port.IFindAllTeststation
import org.slf4j.LoggerFactory

private var logger = LoggerFactory.getLogger(ListOfAllTesterInfo::class.java)


class ListOfAllTesterInfo(
    private val findAllTeststation: IFindAllTeststation,
    private val findAllTester: IFindAllTester
) {
    fun execute(): Map<String, TesterInfo> {
        val allTester = findAllTester.execute()
        val allTeststation = findAllTeststation.execute()
        val result = mutableMapOf<String, TesterInfo>()

        allTester.forEach { t ->
            allTeststation
                .firstOrNull { it.id == t.teststationId }
                ?.let { ts ->
                    result[t.id] =
                        TesterInfo(
                            displayInfo = "${t.person.fullName} in ${ts.name}",
                            testerTeststation = TesterTeststation(tester = t, teststation = ts)
                        )
                }
        }

        logger.debug("ListOfAllTesterInfo(): ${result}")

        return result
    }
}
