package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.Tester
import de.contagio.webapp.repository.mongodb.TesterRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


private var logger = LoggerFactory.getLogger(TesterRestController::class.java)

@CrossOrigin
@RestController
@RequestMapping(TESTER)
open class TesterRestController(
    private val testerRepository: TesterRepository
) {
    @GetMapping
    open fun getTester(): Collection<Tester> {
        logger.debug("getTester()")

        return testerRepository.findAll()
    }

    @GetMapping("/{id}")
    open fun getTester(@PathVariable id: String): ResponseEntity<Tester> {
        logger.debug("getTester(id=$id)")

        val result = testerRepository.findById(id)

        return if (result.isPresent)
            ResponseEntity.ok(result.get())
        else
            ResponseEntity.notFound().build()
    }
}
