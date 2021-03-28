package de.contagio.webapp.restcontroller

import de.brendamour.jpasskit.PKPass
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.usecase.CreatePass
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.CreatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@CrossOrigin
@RestController
@RequestMapping(PASS)
open class PassController(
    private val passInfoRepository: PassInfoRepository,
    private val contagioProperties: ContagioProperties
) {

    private val uidGenerator = UIDGenerator()

    @GetMapping
    open fun getUser(@RequestParam name: String): ResponseEntity<PassInfo> {
        val result = passInfoRepository.findById(name)

        return if (result.isPresent) ResponseEntity.ok(result.get()) else ResponseEntity.notFound().build()
    }

    @GetMapping("/all")
    open fun allUses(): Collection<PassInfo> {
        return passInfoRepository.findAll()
    }

    @PostMapping
    open fun createPass(@RequestBody createPassRequest: CreatePassRequest): ResponseEntity<PKPass> {
        val passInfo = PassInfo(serialNumber = uidGenerator.generate(), userId = createPassRequest.userId)

        if (passInfoRepository.save(passInfo) != null)
            return ResponseEntity.ok(
                CreatePass(
                    teamIdentifier = contagioProperties.teamIdentifier,
                    passTypeIdentifier = contagioProperties.passTypeId,
                    authenticationToken = "0123456789abcdef"
                ).build(passInfo)
            )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
    }
}
