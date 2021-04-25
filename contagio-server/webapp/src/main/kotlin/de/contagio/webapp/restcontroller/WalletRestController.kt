package de.contagio.webapp.restcontroller

import de.contagio.core.domain.entity.DeviceInfo
import de.contagio.core.domain.entity.RegistrationInfo
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.WalletLog
import de.contagio.webapp.model.WalletPasses
import de.contagio.webapp.model.WalletRegistration
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.DeviceInfoRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import de.contagio.webapp.repository.mongodb.RegistrationInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeFormatter
import java.util.*


private var logger = LoggerFactory.getLogger(WalletRestController::class.java)

@CrossOrigin
@RestController
@RequestMapping("$WALLET/v1")
open class WalletRestController(
    private val passInfoRepository: PassInfoRepository,
    private val passRepository: PassRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val registrationInfoRepository: RegistrationInfoRepository,
    private val contagioProperties: ContagioProperties
) {

    private val uidGenerator = UIDGenerator()

    private val lastModifiedDateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)

    @GetMapping("/passes/{passTypeIdentifier}/{serialNumber}")
    open fun getPass(
        @RequestHeader authorization: String,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<ByteArray> {
        logger.debug("getPass(serialNumber=${serialNumber}, authenticationToken=${authorization})")

        val authorizationStatus = isAuthorized(passTypeIdentifier, serialNumber, authorization)
        if (authorizationStatus != HttpStatus.OK)
            return ResponseEntity.status(authorizationStatus).build()

        val passInfo = passInfoRepository.findById(serialNumber)
        val pass = passInfo
            .flatMap {
                if (!it.passId.isNullOrEmpty())
                    passRepository.findById(it.passId!!)
                else
                    Optional.empty()
            }

        return if (pass.isPresent) {

            val lastModified = lastModifiedDateTimeFormatter.format(passInfo.get().updatedUTC)

            logger.debug("getPass(serialNumber=${serialNumber}): lastModified=$lastModified")

            ResponseEntity
                .ok()
                .header("Last-Modified", lastModified)
                .contentType(pkpassMediatype)
                .body(pass.get().data)
        } else
            ResponseEntity.notFound().build()
    }

    @GetMapping("/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}")
    open fun getPasses(
        @PathVariable deviceLibraryIdentifier: String,
        @PathVariable passTypeIdentifier: String,
        @RequestParam passesUpdatedSince: String?
    ): ResponseEntity<WalletPasses> {
        logger.debug("getPasses(deviceLibraryIdentifier=${deviceLibraryIdentifier},passesUpdatedSince=${passesUpdatedSince})")

        if (passTypeIdentifier != contagioProperties.pass.passTypeId)
            return ResponseEntity.badRequest().build()

        val registrations = registrationInfoRepository.findByDeviceLibraryIdentifier(deviceLibraryIdentifier)

        val serialNumbers = registrations.map { it.serialNumber }.toList()

        return if (serialNumbers.isNotEmpty()) {
            ResponseEntity.ok(WalletPasses(lastUpdated = "bla", serialNumbers = serialNumbers))
        } else
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping("/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}/{serialNumber}")
    open fun registerDevice(
        @RequestBody walletRegistration: WalletRegistration,
        @RequestHeader authorization: String,
        @PathVariable deviceLibraryIdentifier: String,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<Void> {
        logger.debug("registerDevice(deviceLibraryIdentifier=${deviceLibraryIdentifier}, serialNumber=${serialNumber}, pushToken=${walletRegistration.pushToken})")

        val authorizationStatus = isAuthorized(passTypeIdentifier, serialNumber, authorization)
        if (authorizationStatus != HttpStatus.OK)
            return ResponseEntity.status(authorizationStatus).build()

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

    @DeleteMapping("/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}/{serialNumber}")
    open fun unregisterDevice(
        @RequestHeader authorization: String,
        @PathVariable deviceLibraryIdentifier: String,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<Void> {
        logger.debug("unregisterDevice(deviceLibraryIdentifier=${deviceLibraryIdentifier},serialNumber=${serialNumber})")

        val authorizationStatus = isAuthorized(passTypeIdentifier, serialNumber, authorization)
        if (authorizationStatus != HttpStatus.OK)
            return ResponseEntity.status(authorizationStatus).build()

        try {
            val registrations = registrationInfoRepository.findByDeviceLibraryIdentifierAndSerialNumber(
                deviceLibraryIdentifier,
                serialNumber
            )

            registrations.forEach {
                registrationInfoRepository.deleteById(it.id)
            }

        } catch (ex: Exception) {
            logger.error("Exception while unregister ${serialNumber}", ex)
        }

        return ResponseEntity.ok().build()
    }

    @PostMapping("/log")
    open fun logErrors(@RequestBody walletLog: WalletLog): ResponseEntity<Void> {
        logger.debug("logErrors(${walletLog.logs})")

        return ResponseEntity.ok().build()
    }

    private fun isAuthorized(passTypeIdentifier: String, serialNumber: String, authorization: String): HttpStatus {
        var result = HttpStatus.OK

        if (passTypeIdentifier != contagioProperties.pass.passTypeId)
            result = HttpStatus.BAD_REQUEST
        else {
            val passInfo = passInfoRepository.findById(serialNumber)

            if (passInfo.isPresent && "ApplePass ${passInfo.get().authToken}" != authorization) {
                logger.debug("authorization ${authorization} differs from expexted value 'ApplePass ${passInfo.get().authToken}'")

                result = HttpStatus.UNAUTHORIZED
            }
        }

        return result
    }
}
