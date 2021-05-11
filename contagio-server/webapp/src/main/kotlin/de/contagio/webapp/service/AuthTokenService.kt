package de.contagio.webapp.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

private var logger = LoggerFactory.getLogger(AuthTokenService::class.java)

data class AuthTokenData(val authToken: String, val serialNumber: String, val created: Instant = Instant.now())

@Service
open class AuthTokenService {

    private var authTokens = mutableMapOf<String, AuthTokenData>()

    open fun getAuthToken(serialNumber: String) =
        authTokens[serialNumber]?.authToken

    open fun setAuthToken(serialNumber: String, authToken: String) {
        authTokens[serialNumber] = AuthTokenData(serialNumber, authToken)
    }

    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    open fun flush() {
        val oldCount = authTokens.size

        val newTokenMap = mutableMapOf<String, AuthTokenData>()

        authTokens.forEach {
            if (it.value.created.plusMillis(2 * 60 * 1000).isAfter(Instant.now())) {
                newTokenMap[it.key] = it.value
            }
        }

        val newCount = newTokenMap.size
        authTokens = newTokenMap

        logger.debug("AuthTokenService.flush() $oldCount -> $newCount")

    }
}
