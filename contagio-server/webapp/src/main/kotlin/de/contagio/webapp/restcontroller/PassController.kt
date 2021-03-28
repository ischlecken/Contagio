package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.usecase.CreatePass
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.CreatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
    open fun getPass(@RequestParam serialNumber: String): ResponseEntity<PassInfo> {
        val result = passInfoRepository.findById(serialNumber)

        return if (result.isPresent) ResponseEntity.ok(result.get()) else ResponseEntity.notFound().build()
    }

    @GetMapping("/all")
    open fun allPass(): Collection<PassInfo> {
        return passInfoRepository.findAll()
    }

    @PostMapping
    open fun createPass(@RequestBody createPassRequest: CreatePassRequest): ResponseEntity<ByteArray> {
        var result: ResponseEntity<ByteArray> = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

        createPassPayload(createPassRequest.userId)?.let {
            result = ResponseEntity.ok().contentType(MediaType("application", "vnd.apple.pkpass")).body(it)
        }

        return result
    }

    @GetMapping("/create")
    open fun createPass(@RequestParam userId: String): ResponseEntity<ByteArray> {
        var result: ResponseEntity<ByteArray> = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

        createPassPayload(userId)?.let {
            result = ResponseEntity.ok().contentType(MediaType("application", "vnd.apple.pkpass")).body(it)
        }

        return result
    }

    private fun createPassPayload(userId: String): ByteArray? {
        var result: ByteArray? = null
        val passInfo = PassInfo(serialNumber = uidGenerator.generate(), userId = userId)

        if (passInfoRepository.save(passInfo) != null) {
            val createPass = CreatePass(
                teamIdentifier = contagioProperties.teamIdentifier,
                passTypeIdentifier = contagioProperties.passTypeId,
                authenticationToken = "0123456789abcdef"
            )

            result = createPass.buildSignedPassPayload(
                contagioProperties.passResourcesDir,
                contagioProperties.keyName,
                contagioProperties.privateKeyPassword,
                contagioProperties.templateName,
                createPass.build(passInfo)
            )
        }

        return result
    }
}
