package de.contagio.core.domain.port

import de.contagio.core.domain.entity.PassInfoEnvelope

fun interface IFindPassInfoEnvelope {
    fun execute(id:String): PassInfoEnvelope?
}

fun interface ISavePassInfoEnvelope {
    fun execute(passInfoEnvelope: PassInfoEnvelope)
}

fun interface IFindAllPassInfoEnvelope {
    fun execute(pageRequest: PageRequest): PagedResult<PassInfoEnvelope>
}
