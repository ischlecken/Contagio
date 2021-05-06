package de.contagio.core.usecase

import de.contagio.core.domain.port.IFindTeststation

class SearchTeststation(
    private val findTeststation: IFindTeststation
) {
    fun execute(id: String) = findTeststation.execute(id)
}
