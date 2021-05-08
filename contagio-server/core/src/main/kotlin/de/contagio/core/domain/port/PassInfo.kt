package de.contagio.core.domain.port

import de.contagio.core.domain.entity.PassInfo

fun interface IFindPassInfo {
    fun execute(id:String): PassInfo?
}


fun interface IFindAllPassInfo {
    fun execute(pageRequest: PageRequest): PagedResult<PassInfo>
}
