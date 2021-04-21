package de.contagio.webapp.restcontroller

import de.contagio.webapp.model.WalletLog
import de.contagio.webapp.model.WalletPasses
import de.contagio.webapp.model.WalletRegistration
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


private var logger = LoggerFactory.getLogger(WalletRestController::class.java)

@CrossOrigin
@RestController
@RequestMapping("$WALLET/v1")
open class WalletRestController(
    private val passInfoRepository: PassInfoRepository,
    private val passRepository: PassRepository,
    private val contagioProperties: ContagioProperties
) {

    @PostMapping("/passes/{passTypeIdentifier}/{serialNumber}")
    open fun getPass(
        @RequestBody walletRegistration: WalletRegistration,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<ByteArray> {
        logger.debug("getPass(serialNumber=${serialNumber}): pushToken=${walletRegistration.pushToken}")

        if (passTypeIdentifier != contagioProperties.pass.passTypeId)
            return ResponseEntity.badRequest().build()

        val pass = passInfoRepository
            .findById(serialNumber)
            .flatMap {
                if (it.deviceLibraryIdentifier != null && !it.passId.isNullOrEmpty())
                    passRepository.findById(it.passId!!)
                else
                    Optional.empty()
            }

        return if (pass.isPresent)
            ResponseEntity.ok().contentType(pkpassMediatype).body(pass.get().data)
        else
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

        val passes = passInfoRepository.findByDeviceLibraryIdentifier(deviceLibraryIdentifier)

        return if (passes.isNotEmpty()) {
            val serialNumbers = passes.map { it.serialNumber }

            ResponseEntity.ok(WalletPasses(lastUpdated = "bla", serialNumbers = serialNumbers))
        } else
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping("/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}/{serialNumber}")
    open fun registerDevice(
        @PathVariable deviceLibraryIdentifier: String,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<Void> {
        logger.debug("registerDevice(deviceLibraryIdentifier=${deviceLibraryIdentifier},serialNumber=${serialNumber})")

        if (passTypeIdentifier != contagioProperties.pass.passTypeId)
            return ResponseEntity.badRequest().build()

        val passInfo = passInfoRepository.findById(serialNumber)
        val returnStatus = passInfo.map {
            when (it.deviceLibraryIdentifier) {
                null -> {
                    passInfoRepository.save(it.copy(deviceLibraryIdentifier = deviceLibraryIdentifier))

                    HttpStatus.CREATED
                }
                deviceLibraryIdentifier -> HttpStatus.OK
                else -> HttpStatus.UNAUTHORIZED
            }
        }

        return ResponseEntity.status(returnStatus.get()).build()
    }

    @DeleteMapping("/devices/{deviceLibraryIdentifier}/registrations/{passTypeIdentifier}/{serialNumber}")
    open fun unregisterDevice(
        @PathVariable deviceLibraryIdentifier: String,
        @PathVariable passTypeIdentifier: String,
        @PathVariable serialNumber: String
    ): ResponseEntity<Void> {
        logger.debug("unregisterDevice(deviceLibraryIdentifier=${deviceLibraryIdentifier},serialNumber=${serialNumber})")

        if (passTypeIdentifier != contagioProperties.pass.passTypeId)
            return ResponseEntity.badRequest().build()

        val passInfo = passInfoRepository.findById(serialNumber)
        val returnStatus = passInfo.map {
            when (it.deviceLibraryIdentifier) {
                null -> {
                    passInfoRepository.save(it.copy(deviceLibraryIdentifier = null, pushToken = null))

                    HttpStatus.OK
                }
                deviceLibraryIdentifier -> HttpStatus.OK
                else -> HttpStatus.UNAUTHORIZED
            }
        }

        return ResponseEntity.status(returnStatus.get()).build()
    }

    @PostMapping("/log")
    open fun logErrors(@RequestBody walletLog: WalletLog): ResponseEntity<Void> {
        logger.debug("logErrors(walletLog=$walletLog)")

        return ResponseEntity.ok().build()
    }
}
