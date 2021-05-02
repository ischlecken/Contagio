package de.contagio.webapp.restcontroller

import de.contagio.core.domain.port.IFindPass
import de.contagio.core.domain.port.IFindPassInfo
import de.contagio.core.domain.port.IFindRegistrations
import de.contagio.core.usecase.SearchPassesSinceLastUpdate
import de.contagio.webapp.model.WalletLog
import de.contagio.webapp.model.WalletPasses
import de.contagio.webapp.model.WalletRegistration
import de.contagio.webapp.service.WalletService
import de.contagio.webapp.service.validate.ValidateApplePass
import de.contagio.webapp.service.validate.ValidatePassTypeIdentifier
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.servlet.http.HttpServletRequest

private var logger = LoggerFactory.getLogger(WalletRestController::class.java)

@CrossOrigin
@RestController
@RequestMapping("$WALLET/v1")
open class WalletRestController(
    private val findPass: IFindPass,
    private val findPassInfo: IFindPassInfo,
    private val findRegistrations: IFindRegistrations,
    private val walletService: WalletService,
    private val searchPassesSinceLastUpdate: SearchPassesSinceLastUpdate
) {

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
            val lastModified = lastModifiedDateTimeFormatter.format(it.passInfo.updated.atZone(ZoneId.of("GMT")))

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

        logger.debug("getPasses(deviceLibraryIdentifier=${deviceLibraryIdentifier}, passesUpdatedSince=${passesUpdatedSince})")

        var serialNumbers = searchPassesSinceLastUpdate
            .execute(deviceLibraryIdentifier)
            .sortedByDescending {
                it.updated
            }

        if (passesUpdatedSince != null) {
            val updatedSince = Instant.ofEpochSecond(passesUpdatedSince.toLong() + 1)
            logger.debug("updatedSince=$updatedSince")

            serialNumbers = serialNumbers.filter {
                logger.debug("  updated=${it.updated}")

                it.updated.isAfter(updatedSince)
            }
        }

        return if (serialNumbers.isNotEmpty()) {
            ResponseEntity.ok(
                WalletPasses(
                    lastUpdated = serialNumbers.first().updated.epochSecond.toString(),
                    serialNumbers = serialNumbers.map { it.serialNumber }
                )
            )
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

        if (findPassInfo.execute(serialNumber) == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        return if (findRegistrations.execute(deviceLibraryIdentifier, serialNumber).isEmpty()) {
            walletService.register(deviceLibraryIdentifier, serialNumber, walletRegistration.pushToken)

            ResponseEntity.status(HttpStatus.CREATED).build()
        } else
            ResponseEntity.ok().build()
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

        return if (walletService.unregister(deviceLibraryIdentifier, serialNumber))
            ResponseEntity.ok().build()
        else
            ResponseEntity.notFound().build()
    }

    @PostMapping("/log")
    open fun logErrors(@RequestBody walletLog: WalletLog): ResponseEntity<Void> {
        logger.debug("logErrors(${walletLog.logs})")

        return ResponseEntity.ok().build()
    }

}
