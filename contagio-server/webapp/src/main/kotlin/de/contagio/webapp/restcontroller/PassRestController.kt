package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.PassInfoEnvelope
import de.contagio.core.domain.entity.PassType
import de.contagio.core.domain.entity.TestResultType
import de.contagio.core.domain.entity.TestType
import de.contagio.core.domain.port.IFindEncryptedPayload
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.IdType
import de.contagio.core.usecase.SearchTesterWithTeststation
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassInfoEnvelopeRepository
import de.contagio.webapp.service.PassCommandProcessor
import de.contagio.webapp.service.SignDataService
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
    private val passInfoEnvelopeRepository: PassInfoEnvelopeRepository,
    private val passCommandProcessor: PassCommandProcessor,
    private val signDataService: SignDataService,
    private val searchTesterWithTeststation: SearchTesterWithTeststation,
    private val qrCodeGeneratorService: QRCodeGeneratorService,
    private val urlBuilder: UrlBuilder,
    private val getEncryptionKey: IGetEncryptionKey,
    private val findEncryptedPayload: IFindEncryptedPayload,
    private val contagioProperties: ContagioProperties
) {


    @GetMapping("/info")
    open fun getPassEnvelopes(pageable: Pageable): Page<PassInfoEnvelope> {
        logger.debug("getPassEnvelopes()")

        return passInfoEnvelopeRepository.findAll(pageable)
    }

    @GetMapping("/info/{serialNumber}")
    open fun getPassInfoEnvelope(@PathVariable serialNumber: String): ResponseEntity<PassInfoEnvelope> {
        logger.debug("getPassInfoEnvelope($serialNumber)")

        val result = passInfoEnvelopeRepository.findById(serialNumber)

        return if (result.isPresent)
            ResponseEntity.ok(result.get())
        else
            ResponseEntity.notFound().build()
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
    ): ResponseEntity<PassInfoEnvelope> {

        val passType = when (testType) {
            TestType.VACCINATION -> PassType.COUPON
            TestType.PCRTEST -> PassType.EVENT
            TestType.RAPIDTEST -> PassType.COUPON
        }
        val labelColor = when (testType) {
            TestType.VACCINATION -> "rgb(0, 255, 0)"
            else -> contagioProperties.pass.labelColor
        }
        val foregroundColor = contagioProperties.pass.foregroundColor
        val backgroundColor = when (testType) {
            TestType.VACCINATION -> "rgb(44, 133, 75)"
            else -> contagioProperties.pass.backgroundColor
        }

        return searchTesterWithTeststation.execute(testerId)?.let {
            passCommandProcessor.createPass(
                image,
                firstName, lastName,
                phoneNo, email,
                it.tester.id,
                testResult, testType,
                passType, labelColor, foregroundColor, backgroundColor,
                save = true
            ).let { cpr ->
                ResponseEntity.status(HttpStatus.CREATED).body(cpr?.passInfoEnvelope)
            } ?: ResponseEntity.badRequest().build()
        } ?: ResponseEntity.badRequest().build()
    }

    @PatchMapping("/info")
    open fun updatePass(@RequestBody updatePassRequest: UpdatePassRequest): ResponseEntity<PassInfoEnvelope> {

        val passInfo = passCommandProcessor.updatePass(updatePassRequest)

        return if (passInfo != null)
            ResponseEntity.ok(passInfo)
        else
            ResponseEntity.notFound().build()
    }


    @GetMapping("/image/{id}")
    open fun getPassImage(@PathVariable id: String): ResponseEntity<ByteArray> {
        val encryptedImage = findEncryptedPayload.execute(id)

        val result = encryptedImage?.get(getEncryptionKey.execute(IdType.IMAGEID, id))

        logger.debug("getPassImage($id) ${result != null}")

        return if (result != null) {
            ResponseEntity.ok().contentType(MediaType.parseMediaType("image/jpeg")).body(result)
        } else
            ResponseEntity.notFound().build()
    }

    @GetMapping("/{passId}")
    open fun getPass(@PathVariable passId: String): ResponseEntity<ByteArray> {
        val encryptedPass = findEncryptedPayload.execute(passId)
        val result = encryptedPass?.get(getEncryptionKey.execute(IdType.PASSID, passId))

        logger.debug("getPass(passId=$passId): ${result != null}")

        if (result == null)
            return ResponseEntity.notFound().build()

        return ResponseEntity.ok().contentType(pkpassMediatype).body(result)
    }

    @GetMapping("/{passId}/signature")
    open fun getPassSignature(@PathVariable passId: String): ResponseEntity<ByteArray> {
        val encryptedPass = findEncryptedPayload.execute(passId)
        val result = encryptedPass?.get(getEncryptionKey.execute(IdType.PASSID, passId))

        logger.debug("getPassSignature(passId=$passId): ${result != null}")

        if (result == null)
            return ResponseEntity.notFound().build()

        val s = signDataService.sign(result)

        return if (s != null)
            ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(s)
        else
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

    }

    @GetMapping("/{passId}/qrcode")
    open fun getPassQRCode(@PathVariable passId: String): ResponseEntity<ByteArray> {
        val encryptedPass = findEncryptedPayload.execute(passId)
        val result = encryptedPass?.get(getEncryptionKey.execute(IdType.PASSID, passId))

        logger.debug("getPassQRCode(passId=$passId): ${result != null}")

        if (result == null)
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
