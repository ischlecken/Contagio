package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.DeviceInfo
import de.contagio.core.domain.entity.RegistrationInfo
import de.contagio.core.domain.port.IFindPass
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.WalletLog
import de.contagio.webapp.model.WalletPasses
import de.contagio.webapp.model.WalletRegistration
import de.contagio.webapp.repository.mongodb.DeviceInfoRepository
import de.contagio.webapp.repository.mongodb.RegistrationInfoRepository
import de.contagio.webapp.service.validate.ValidateApplePass
import de.contagio.webapp.service.validate.ValidatePassTypeIdentifier
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeFormatter
import java.util.*
import javax.servlet.http.HttpServletRequest


private var logger = LoggerFactory.getLogger(WalletRestController::class.java)

@CrossOrigin
@RestController
@RequestMapping("$WALLET/v1")
open class WalletRestController(
    private val findPass: IFindPass,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val registrationInfoRepository: RegistrationInfoRepository
) {

    private val uidGenerator = UIDGenerator()

    private val lastModifiedDateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)

    @ValidateApplePass
    @GetMapping("/passes/{passTypeIdentifier}/{serialNumber}")
    open fun getPass(
        request: HttpServletRequest,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<ByteArray> {
        logger.debug("getPass(serialNumber=${serialNumber})")

        return findPass.execute(serialNumber)?.let {
            val lastModified = lastModifiedDateTimeFormatter.format(it.passInfo.updatedUTC)

            logger.debug("getPass(serialNumber=${serialNumber}): lastModified=$lastModified")

            ResponseEntity
                .ok()
                .header("Last-Modified", lastModified)
                .contentType(pkpassMediatype)
                .body(it.pass.data)
        } ?: ResponseEntity.notFound().build()
    }

    @ValidatePassTypeIdentifier
    @GetMapping("/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}")
    open fun getPasses(
        @PathVariable deviceLibraryIdentifier: String,
        @PathVariable passTypeIdentifier: String,
        @RequestParam passesUpdatedSince: String?
    ): ResponseEntity<WalletPasses> {
        logger.debug("getPasses(deviceLibraryIdentifier=${deviceLibraryIdentifier},passesUpdatedSince=${passesUpdatedSince})")

        val registrations = registrationInfoRepository.findByDeviceLibraryIdentifier(deviceLibraryIdentifier)
        val serialNumbers = registrations.map { it.serialNumber }.toList()

        return if (serialNumbers.isNotEmpty()) {
            ResponseEntity.ok(WalletPasses(lastUpdated = "bla", serialNumbers = serialNumbers))
        } else
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @ValidateApplePass
    @PostMapping("/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}/{serialNumber}")
    open fun registerDevice(
        request: HttpServletRequest,
        @RequestBody walletRegistration: WalletRegistration,
        @PathVariable deviceLibraryIdentifier: String,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<Void> {
        logger.debug("registerDevice(deviceLibraryIdentifier=${deviceLibraryIdentifier}, serialNumber=${serialNumber}, pushToken=${walletRegistration.pushToken})")

        val registrations = registrationInfoRepository.findByDeviceLibraryIdentifierAndSerialNumber(
            deviceLibraryIdentifier,
            serialNumber
        )
        var returnStatus = HttpStatus.OK
        if (registrations.isEmpty()) {
            registrationInfoRepository.save(
                RegistrationInfo(
                    id = uidGenerator.generate(),
                    deviceLibraryIdentifier = deviceLibraryIdentifier,
                    serialNumber = serialNumber
                )
            )

            deviceInfoRepository.save(
                DeviceInfo(
                    deviceLibraryIdentifier = deviceLibraryIdentifier,
                    pushToken = walletRegistration.pushToken
                )
            )

            returnStatus = HttpStatus.CREATED
        }

        return ResponseEntity.status(returnStatus).build()
    }

    @ValidateApplePass
    @DeleteMapping("/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}/{serialNumber}")
    open fun unregisterDevice(
        request: HttpServletRequest,
        @PathVariable deviceLibraryIdentifier: String,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<Void> {
        logger.debug("unregisterDevice(deviceLibraryIdentifier=${deviceLibraryIdentifier},serialNumber=${serialNumber})")

        try {
            val registrations = registrationInfoRepository.findByDeviceLibraryIdentifierAndSerialNumber(
                deviceLibraryIdentifier,
                serialNumber
            )

            registrations.forEach {
                registrationInfoRepository.deleteById(it.id)
            }

        } catch (ex: Exception) {
            logger.error("Exception while unregister $serialNumber", ex)
        }

        return ResponseEntity.ok().build()
    }

    @PostMapping("/log")
    open fun logErrors(@RequestBody walletLog: WalletLog): ResponseEntity<Void> {
        logger.debug("logErrors(${walletLog.logs})")

        return ResponseEntity.ok().build()
    }

}
