package de.contagio.core.domain.port

import de.contagio.core.domain.entity.PassInfoEnvelope

fun interface IFindPassInfo {
    fun execute(id:String): PassInfoEnvelope?
}


fun interface IFindAllPassInfo {
    fun execute(pageRequest: PageRequest): PagedResult<PassInfoEnvelope>
}
