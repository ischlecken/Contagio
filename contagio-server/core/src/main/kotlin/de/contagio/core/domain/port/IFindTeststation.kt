package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Teststation

fun interface IFindTeststation {
    fun execute(id:String): Teststation?
}
