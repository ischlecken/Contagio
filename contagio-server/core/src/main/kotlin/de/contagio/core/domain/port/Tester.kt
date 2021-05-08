package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.entity.TesterTeststation

fun interface IFindTester {
    fun execute(id: String): Tester?
}

fun interface IFindAllTester {
    fun execute(pageRequest: PageRequest): PagedResult<Tester>
}


fun interface ISaveTester {
    fun execute(tester: Tester)
}


fun interface IDeleteTester {
    fun execute(tester: Tester)
}
