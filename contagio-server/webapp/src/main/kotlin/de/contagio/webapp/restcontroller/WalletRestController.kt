package de.contagio.webapp.restcontroller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


private var logger = LoggerFactory.getLogger(WalletRestController::class.java)

@CrossOrigin
@RestController
@RequestMapping(WALLET)
class WalletRestController {

    @GetMapping
    open fun getInfo(): ResponseEntity<Void> {
        logger.debug("getInfo()")

        return ResponseEntity.ok().build()
    }
}
