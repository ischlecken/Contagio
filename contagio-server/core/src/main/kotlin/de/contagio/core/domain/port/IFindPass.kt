package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Pass
import de.contagio.core.domain.entity.PassInfoEnvelope

data class PassInfoPass(val passInfoEnvelope: PassInfoEnvelope, val pass: Pass)

fun interface IFindPass {
    fun execute(id: String): PassInfoPass?
}
