package de.contagio.core.usecase

import de.contagio.core.domain.entity.TesterTeststation
import de.contagio.core.domain.port.IFindAllTester
import de.contagio.core.domain.port.IFindAllTeststation
import de.contagio.core.domain.port.PageRequest
import de.contagio.core.domain.port.PagedResult

class SearchAllTesterTeststation(
    private val findAllTeststation: IFindAllTeststation,
    private val findAllTester: IFindAllTester
) {
    fun execute(pageRequest: PageRequest): PagedResult<TesterTeststation> {
        val allTeststation = findAllTeststation.execute(PageRequest(0, 1000))
        val allTester = findAllTester.execute(pageRequest)
        val result = mutableListOf<TesterTeststation>()

        allTester.content.forEach { t ->
            allTeststation
                .content
                .firstOrNull { it.id == t.teststationId }
                ?.let { ts ->
                    result.add(
                        TesterTeststation(
                            tester = t,
                            teststation = ts
                        )
                    )
                }
        }

        return PagedResult(
            content = result,
            isFirst = allTester.isFirst,
            isLast = allTester.isLast,
            pageSize = pageRequest.pageSize,
            pageNo = pageRequest.pageNo,
            totalPages = allTester.totalPages,
            totalElements = allTester.totalElements
        )
    }
}
