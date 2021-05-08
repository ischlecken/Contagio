package de.contagio.core.usecase

import de.contagio.core.domain.entity.TesterInfo
import de.contagio.core.domain.entity.TesterTeststation
import de.contagio.core.domain.port.IFindAllTester
import de.contagio.core.domain.port.IFindAllTeststation
import de.contagio.core.domain.port.PageRequest

class ListOfAllTesterInfo(
    private val findAllTeststation: IFindAllTeststation,
    private val findAllTester: IFindAllTester
) {
    fun execute(): Map<String, TesterInfo> {
        val allTester = findAllTester.execute(PageRequest(0, 1000))
        val allTeststation = findAllTeststation.execute(PageRequest(0, 1000))
        val result = mutableMapOf<String, TesterInfo>()

        allTester.content.forEach { t ->
            allTeststation
                .content
                .firstOrNull { it.id == t.teststationId }
                ?.let { ts ->
                    result[t.id] =
                        TesterInfo(
                            displayInfo = "${t.person.fullName} in ${ts.name}",
                            testerTeststation = TesterTeststation(tester = t, teststation = ts)
                        )
                }
        }

        return result
    }
}
