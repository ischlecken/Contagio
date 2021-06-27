package de.contagio.webapp.restcontroller

import de.contagio.core.domain.port.IDeleteDeviceToken
import de.contagio.core.usecase.RegisterDeviceToken
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

private var logger = LoggerFactory.getLogger(AppRestController::class.java)

@CrossOrigin
@RestController
@RequestMapping("$APPS")
open class AppRestController(
    private val registerDeviceToken: RegisterDeviceToken,
    private val deleteDeviceToken: IDeleteDeviceToken
) {

    @PostMapping("/registration/{bundleId}/{deviceToken}")
    open fun registerDeviceToken(
        @RequestBody serialNumbers: Collection<String>,
        @PathVariable bundleId: String,
        @PathVariable deviceToken: String
    ) {
        logger.debug("registerDeviceToken(deviceToken=${deviceToken},bundleId=${bundleId}):${serialNumbers}")

        registerDeviceToken.execute(deviceToken, bundleId, serialNumbers)
    }

    @DeleteMapping("/registration/{bundleId}/{deviceToken}")
    open fun unregisterDeviceToken(
        @PathVariable bundleId: String,
        @PathVariable deviceToken: String
    ) {
        logger.debug("unregisterDeviceToken(deviceToken=${deviceToken})")

        deleteDeviceToken.execute(deviceToken)
    }

}
