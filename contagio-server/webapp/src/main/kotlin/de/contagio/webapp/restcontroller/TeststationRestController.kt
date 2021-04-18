package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.entity.Teststation
import de.contagio.webapp.repository.mongodb.TeststationRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private var logger = LoggerFactory.getLogger(TeststationRestController::class.java)


@CrossOrigin
@RestController
@RequestMapping(TESTSTATION)
open class TeststationRestController(
    private val teststationRepository: TeststationRepository
) {
    @GetMapping
    open fun getTeststations(): Collection<Teststation> {
        logger.debug("getTeststations()")

        return teststationRepository.findAll()
    }

    @GetMapping("/{id}")
    open fun getTeststation(@PathVariable id: String): ResponseEntity<Teststation> {
        logger.debug("getTeststation(id=$id)")

        val result = teststationRepository.findById(id)

        return if (result.isPresent)
            ResponseEntity.ok(result.get())
        else
            ResponseEntity.notFound().build()
    }
}
