package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Tester

fun interface IFindTester {
    fun execute(id: String): Tester?
}

fun interface IFindAllTester {
    fun execute(): Collection<Tester>
}


fun interface ISaveTester {
    fun execute(tester: Tester)
}


fun interface IDeleteTester {
    fun execute(tester: Tester)
}
