package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Teststation

fun interface IFindTeststation {
    fun execute(id:String): Teststation?
}


fun interface IFindAllTeststation {
    fun execute(): Collection<Teststation>
}

fun interface ISaveTeststation {
    fun execute(teststation: Teststation)
}


fun interface IDeleteTeststation {
    fun execute(teststation: Teststation)
}
