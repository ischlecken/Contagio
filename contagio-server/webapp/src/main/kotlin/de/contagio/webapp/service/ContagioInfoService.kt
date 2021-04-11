package de.contagio.webapp.service

import de.contagio.webapp.repository.mongodb.PassInfoRepository
import org.springframework.stereotype.Service

@Service
    open class ContagioInfoService(
    private val passInfoRepository: PassInfoRepository
) {

    val activePassCount: Long
        get() = passInfoRepository.count()
}
