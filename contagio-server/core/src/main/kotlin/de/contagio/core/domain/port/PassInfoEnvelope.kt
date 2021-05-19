package de.contagio.core.domain.port

import de.contagio.core.domain.entity.PassInfoEnvelope
import de.contagio.core.domain.entity.PassUpdateLog

fun interface IFindPassInfoEnvelope {
    fun execute(id:String): PassInfoEnvelope?
}

fun interface ISavePassInfoEnvelope {
    fun execute(passInfoEnvelope: PassInfoEnvelope)
}

fun interface IFindAllPassInfoEnvelope {
    fun execute(pageRequest: PageRequest): PagedResult<PassInfoEnvelope>
}

fun interface IDeletePassInfoEnvelope {
    fun execute(id:String?)
}

fun interface IFindAllPassUpdateLog {
    fun execute(pageRequest: PageRequest): PagedResult<PassUpdateLog>
}
fun interface ISavePassUpdateLog {
    fun execute(passUpdateLog: PassUpdateLog)
}
