package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.entity.TestResultType
import de.contagio.core.domain.entity.TestType
import de.contagio.core.usecase.SearchTesterWithTeststation
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.repository.mongodb.PassImageRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import de.contagio.webapp.service.PassBuilderService
import de.contagio.webapp.service.PassService
import de.contagio.webapp.service.QRCodeGeneratorService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

private var logger = LoggerFactory.getLogger(PassRestController::class.java)


@CrossOrigin
@RestController
@RequestMapping(PASS)
open class PassRestController(
    private val passInfoRepository: PassInfoRepository,
    private val passImageRepository: PassImageRepository,
    private val passRepository: PassRepository,
    private val passService: PassService,
    private val passBuilderService: PassBuilderService,
    private val searchTesterWithTeststation: SearchTesterWithTeststation,
    private val qrCodeGeneratorService: QRCodeGeneratorService,
    private val urlBuilder: UrlBuilder
) {


    @GetMapping("/info")
    open fun getPasses(pageable: Pageable): Page<PassInfo> {
        logger.debug("getAllPassInfo()")

        return passInfoRepository.findAll(pageable)
    }

    @GetMapping("/info/{serialNumber}")
    open fun getPassInfo(@PathVariable serialNumber: String): ResponseEntity<PassInfo> {
        logger.debug("getPassInfo($serialNumber)")

        val result = passInfoRepository.findById(serialNumber)

        return if (result.isPresent) ResponseEntity.ok(result.get()) else ResponseEntity.notFound().build()
    }

    @PostMapping("/info")
    open fun createPass(
        @RequestParam image: MultipartFile,
        @RequestParam firstName: String,
        @RequestParam lastName: String,
        @RequestParam phoneNo: String,
        @RequestParam email: String?,
        @RequestParam testerId: String,
        @RequestParam testResult: TestResultType,
        @RequestParam testType: TestType
    ): ResponseEntity<PassInfo> {

        return searchTesterWithTeststation.execute(testerId)?.let {
            passService.createPassAndSave(
                image,
                firstName, lastName,
                phoneNo, email,
                it.teststation.id,
                it.tester.id,
                testResult, testType
            ).let { cpr ->
                ResponseEntity.status(HttpStatus.CREATED).body(cpr.passInfo)
            } ?: ResponseEntity.badRequest().build()
        } ?: ResponseEntity.badRequest().build()
    }

    @PatchMapping("/info")
    open fun updatePass(@RequestBody updatePassRequest: UpdatePassRequest): ResponseEntity<PassInfo> {

        val passInfo = passService.updatePass(updatePassRequest)

        return if (passInfo != null)
            ResponseEntity.ok(passInfo)
        else
            ResponseEntity.notFound().build()
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
        val result = passRepository.findById(passId)

        logger.debug("getPass(passId=$passId): ${result.isPresent}")

        if (result.isEmpty)
            return ResponseEntity.notFound().build()

        return ResponseEntity.ok().contentType(pkpassMediatype).body(result.get().data)
    }

    @GetMapping("/{passId}/signature")
    open fun getPassSignature(@PathVariable passId: String): ResponseEntity<ByteArray> {
        val result = passRepository.findById(passId)

        logger.debug("getPassSignature(passId=$passId): ${result.isPresent}")

        if (result.isEmpty)
            return ResponseEntity.notFound().build()

        val s = passBuilderService.sign(result.get())

        return if (s != null)
            ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(s)
        else
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

    }

    @GetMapping("/{passId}/qrcode")
    open fun getPassQRCode(@PathVariable passId: String): ResponseEntity<ByteArray> {
        val result = passRepository.findById(passId)

        logger.debug("getPassQRCode(passId=$passId): ${result.isPresent}")

        if (result.isEmpty)
            return ResponseEntity.notFound().build()

        var qrCode: ByteArray? = null
        try {
            qrCode = qrCodeGeneratorService.generate(urlBuilder.passURL(passId), 400, 400)
        } catch (ex: Exception) {
            logger.error("Error while generationg qrcode", ex)
        }

        return if (qrCode != null)
            ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCode)
        else
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

    }
}
