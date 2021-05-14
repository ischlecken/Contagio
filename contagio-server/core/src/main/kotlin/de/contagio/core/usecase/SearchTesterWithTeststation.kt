package de.contagio.core.usecase

import de.contagio.core.domain.entity.TesterTeststation
import de.contagio.core.domain.port.IFindTester
import de.contagio.core.domain.port.IFindTeststation

class SearchTesterWithTeststation(
    private val findTester: IFindTester,
    private val findTeststation: IFindTeststation
) {

    fun execute(id: String?): TesterTeststation? {
        var result: TesterTeststation? = null

        if (id != null)
            findTester.execute(id)?.let { tester ->
                findTeststation.execute(tester.teststationId)?.let { teststation ->
                    result = TesterTeststation(
                        tester = tester,
                        teststation = teststation
                    )
                }
            }

        return result
    }
}
