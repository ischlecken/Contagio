package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Tester

fun interface IFindTester {
    fun execute(id:String): Tester?
}
