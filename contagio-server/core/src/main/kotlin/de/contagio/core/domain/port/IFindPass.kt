package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Pass
import de.contagio.core.domain.entity.PassInfo

data class PassInfoPass(val passInfo: PassInfo, val pass: Pass)

fun interface IFindPass {
    fun execute(id: String): PassInfoPass?
}
