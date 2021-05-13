package de.contagio.webapp.service

import de.contagio.webapp.repository.mongodb.PassInfoEnvelopeRepository
import org.springframework.stereotype.Service

@Service
    open class ContagioInfoService(
    private val passInfoEnvelopeRepository: PassInfoEnvelopeRepository
) {

    val activePassCount: Long
        get() = passInfoEnvelopeRepository.count()
}
