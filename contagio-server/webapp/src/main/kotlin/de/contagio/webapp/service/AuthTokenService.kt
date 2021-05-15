package de.contagio.webapp.service

import de.contagio.core.domain.port.IdType
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

private var logger = LoggerFactory.getLogger(AuthTokenService::class.java)


data class AuthTokenData(
    val type: IdType,
    val id: String,
    val authToken: String,
    val created: Instant = Instant.now()
)

@Service
open class AuthTokenService {

    private var authTokens = mutableMapOf<String, AuthTokenData>()

    open fun getAuthToken(type: IdType, id: String): String? {
        val result = authTokens["${type.name}_$id"]?.authToken

        logger.debug("getAuthToken(type=$type, id=$id): $result")

        return result
    }


    open fun setAuthToken(type: IdType, id: String, authToken: String) {
        logger.debug("setAuthToken(type=$type, id=$id, authToken=$authToken)")

        authTokens["${type.name}_$id"] = AuthTokenData(type, id, authToken)
    }

    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    open fun flush() {
        val oldCount = authTokens.size

        val newTokenMap = mutableMapOf<String, AuthTokenData>()

        authTokens.forEach {
            if (it.value.created.plusMillis(10 * 60 * 1000).isAfter(Instant.now())) {
                newTokenMap[it.key] = it.value
            }
        }

        val newCount = newTokenMap.size
        authTokens = newTokenMap

        logger.debug("AuthTokenService.flush() $oldCount -> $newCount")

    }
}
