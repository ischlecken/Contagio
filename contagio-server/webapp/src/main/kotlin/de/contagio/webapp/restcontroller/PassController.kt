package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.*
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.CreatePassRequest
import de.contagio.webapp.repository.mongodb.PassImageRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import de.contagio.webapp.service.PassBuilder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

private var logger = LoggerFactory.getLogger(PassController::class.java)


@CrossOrigin
@RestController
@RequestMapping(PASS)
open class PassController(
    private val passInfoRepository: PassInfoRepository,
    private val passImageRepository: PassImageRepository,
    private val passRepository: PassRepository,
    private val passBuilder: PassBuilder
) {
    private val uidGenerator = UIDGenerator()

    @GetMapping("/info/all")
    open fun getAllPass(): Collection<PassInfo> {
        logger.debug("getAllPass()")

        return passInfoRepository.findAll()
    }

    @GetMapping("/info/{serialNumber}")
    open fun getPassInfo(@PathVariable serialNumber: String): ResponseEntity<PassInfo> {
        logger.debug("getPassInfo($serialNumber)")

        val result = passInfoRepository.findById(serialNumber)

        return if (result.isPresent) ResponseEntity.ok(result.get()) else ResponseEntity.notFound().build()
    }


    @GetMapping("/image/{id}")
    open fun getPassImage(@PathVariable id: String): ResponseEntity<ByteArray> {
        val result = passImageRepository.findById(id)

        logger.debug("getPassImage($id)")

        return if (result.isPresent)
            ResponseEntity.ok().contentType(MediaType.parseMediaType(result.get().type)).body(result.get().data)
        else
            ResponseEntity.notFound().build()
    }

    @GetMapping("/{passId}")
    open fun getPass(@PathVariable passId: String): ResponseEntity<ByteArray> {
        logger.debug("getPass(passId=$passId)")

        val result = passRepository.findById(passId)

        return if (result.isPresent)
            ResponseEntity.ok().contentType(MediaType("application", "vnd.apple.pkpass")).body(result.get().data)
        else
            ResponseEntity.notFound().build()
    }


    @PostMapping()
    open fun createPass(
        @RequestParam image: MultipartFile,
        @RequestParam firstName: String,
        @RequestParam lastName: String,
        @RequestParam phoneNo: String,

        @RequestParam teststationId: String,
        @RequestParam testerId: String,

        @RequestParam testResult: TestResultType,
        @RequestParam testType: TestType
    ): ResponseEntity<PassInfo> {
        logger.debug("createPass(firstName=$firstName, lastName=$lastName, testResult=$testResult)")
        logger.debug("  image.size=${image.size}")


        val passInfo = PassInfo(
            serialNumber = uidGenerator.generate(),
            person = Person(firstName = firstName, lastName = lastName, phoneNo = phoneNo),
            imageId = uidGenerator.generate(),
            authToken = uidGenerator.generate(),
            testResult = testResult,
            testType = testType,
            issueStatus = IssueStatus.CREATED,
            testerId = testerId,
            teststationId = teststationId
        )

        val passImage = PassImage(
            id = passInfo.imageId,
            type = image.contentType ?: "",
            data = image.bytes
        )

        val createPassRequest = CreatePassRequest(
            passInfo = passInfo,
            passImage = passImage
        )

        return passBuilder.build(createPassRequest)?.let {
            ResponseEntity.status(HttpStatus.CREATED).body(it)
        } ?: ResponseEntity.badRequest().build()
    }

}
