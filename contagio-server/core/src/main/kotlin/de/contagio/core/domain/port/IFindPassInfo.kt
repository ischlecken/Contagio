package de.contagio.core.domain.port

import de.contagio.core.domain.entity.PassInfo

fun interface IFindPassInfo {
    fun execute(id:String): PassInfo?
}
