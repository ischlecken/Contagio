package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Tester

fun interface IFindAllTester {
    fun execute(): Collection<Tester>
}
