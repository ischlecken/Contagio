package de.contagio.core.domain.port

import de.contagio.core.domain.entity.Teststation


fun interface IFindAllTeststation {
    fun execute(): Collection<Teststation>
}
