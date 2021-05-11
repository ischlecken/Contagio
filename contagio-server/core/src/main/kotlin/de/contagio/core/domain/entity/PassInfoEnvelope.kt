package de.contagio.core.domain.entity

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.contagio.core.usecase.Decryptor
import de.contagio.core.usecase.Encryptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import java.time.Instant
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("de.contagio.core.domain.entity")

private val om = jacksonObjectMapper().apply {
    val javaTimeModule = JavaTimeModule()

    javaTimeModule.addSerializer(
        Instant::class.java,
        object : InstantSerializer(
            INSTANCE,
            false,
            DateTimeFormatterBuilder().appendInstant(0).toFormatter()
        ) {
        }
    )
    registerModule(javaTimeModule)
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}

enum class TestResultType {
    UNKNOWN, POSITIVE, NEGATIVE
}

enum class TestType {
    RAPIDTEST, PCRTEST, VACCINATION
}


enum class PassType {
    GENERIC, COUPON, EVENT, STORE
}

enum class IssueStatus {
    CREATED {
        override fun isActive() = true
    },
    ISSUED {
        override fun isActive() = true
    },
    EXPIRED {
        override fun isActive() = false
    },
    REVOKED {
        override fun isActive() = false
    },
    REFUSED {
        override fun isActive() = false
    };

    abstract fun isActive(): Boolean
}


enum class PassInstallationStatus {
    PENDING, INSTALLED, REMOVED
}

data class PassImage(
    @Id val id: String,
    val iv: String,
    val data: ByteArray,
    val type: String,
    val created: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PassImage

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false
        if (iv != other.iv) return false
        if (type != other.type) return false
        if (created != other.created) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + iv.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + created.hashCode()
        return result
    }

    companion object {
        fun build(
            authToken: String,
            imageId: String,
            data: ByteArray,
            contentType: String?
        ): PassImage {
            val encryptor = Encryptor()
            val iv = Base64.getEncoder().encodeToString(encryptor.generateIV().iv)

            return PassImage(
                id = imageId,
                type = contentType ?: "",
                data = encryptor.execute(data, authToken, iv),
                iv = iv
            )
        }
    }
}


data class Pass(
    @Id val id: String,
    val data: ByteArray,
    val created: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pass

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false
        if (created != other.created) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + created.hashCode()
        return result
    }
}


data class PassInfo(
    val passId: String? = null,
    val imageId: String,
    val person: Person,
    val testResult: TestResultType,
    val testType: TestType,
    val passType: PassType,
    val description: String,
    val logoText: String,
    val labelColor: String,
    val foregroundColor: String,
    val backgroundColor: String,
    val validUntil: Instant? = null
) {

    fun toEncryptedPayload(authToken: String, iv: String): ByteArray {
        val json = om.writeValueAsBytes(this)

        return Encryptor().execute(json, authToken, iv)
    }

    companion object {
        fun toPassInfo(data: ByteArray, authToken: String, iv: String): PassInfo? {
            var result: PassInfo? = null

            try {
                val decryptedData = Decryptor().execute(data, authToken, iv)

                result = om.readValue<PassInfo>(decryptedData)
            } catch (ex: Exception) {
                logger.error("Error while decrypting passinfo payload", ex)
            }

            return result
        }
    }
}


@Suppress("ArrayInDataClass")
data class PassInfoEnvelope(
    @Id val serialNumber: String,
    val iv: String,
    val passInfoPayload: ByteArray,
    val teststationId: String,
    val testerId: String,
    val issueStatus: IssueStatus,
    val version: Int = 0,
    val created: Instant = Instant.now(),
    val modified: Instant? = null,
    val passInstallationStatus: PassInstallationStatus = PassInstallationStatus.PENDING,
    val passInstalled: Instant? = null,
    val passRemoved: Instant? = null

) {
    val updated: Instant get() = modified ?: created

    fun update(
        authToken: String,
        testResult: TestResultType,
        issueStatus: IssueStatus,
        validUntil: Instant? = null
    ): PassInfoEnvelope? {

        return getPassInfo(authToken)?.let {
            val newValidUntil = when {
                issueStatus == IssueStatus.EXPIRED -> it.validUntil
                validUntil != null -> validUntil
                else -> Instant.now()
                    .plus(if (it.testType == TestType.VACCINATION) 24 * 364 else 1, ChronoUnit.DAYS)
            }

            it.copy(
                testResult = testResult,
                validUntil = newValidUntil
            )
        }?.let {
            copy(
                issueStatus = issueStatus,
                modified = Instant.now(),
                version = version + 1,
                passInfoPayload = it.toEncryptedPayload(authToken, iv)
            )
        }
    }

    fun getPassInfo(authToken: String): PassInfo? {
        return PassInfo.toPassInfo(passInfoPayload, authToken, iv)
    }
}

@Suppress("ArrayInDataClass")
data class ExtendedPassInfo(
    val passInfoEnvelope: PassInfoEnvelope,
    val testerTeststation: TesterTeststation,
    val passInfo: PassInfo? = null
)
