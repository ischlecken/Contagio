package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.Instant
import java.time.temporal.ChronoUnit

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
        if (type != other.type) return false
        if (created != other.created) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + created.hashCode()
        return result
    }

    companion object {
        fun build(
            data: ByteArray,
            contentType: String?,
            passInfo: PassInfo
        ): PassImage {
            return PassImage(
                id = passInfo.imageId,
                type = contentType ?: "",
                data = data
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
    @Id val serialNumber: String,
    val person: Person,
    val imageId: String,
    val teststationId: String,
    val testerId: String,
    val authToken: String,
    val testResult: TestResultType,
    val testType: TestType,
    val issueStatus: IssueStatus,
    val passType: PassType,
    val description: String,
    val logoText: String,
    val labelColor: String,
    val foregroundColor: String,
    val backgroundColor: String,
    val passId: String? = null,
    val passInstallationStatus: PassInstallationStatus = PassInstallationStatus.PENDING,
    val version: Int = 0,
    val created: Instant = Instant.now(),
    val modified: Instant? = null,
    val validUntil: Instant? = null,
    val passInstalled: Instant? = null,
    val passRemoved: Instant? = null

) {
    val updated: Instant get() = modified ?: created

    fun update(
        testResult: TestResultType,
        issueStatus: IssueStatus,
        validUntil: Instant? = null
    ): PassInfo {

        val newValidUntil = when {
            issueStatus == IssueStatus.EXPIRED -> this.validUntil
            validUntil != null -> validUntil
            else -> Instant.now()
                .plus(if (testType == TestType.VACCINATION) 24 * 364 else 1, ChronoUnit.DAYS)
        }

        return this.copy(
            testResult = testResult,
            issueStatus = issueStatus,
            modified = Instant.now(),
            validUntil = newValidUntil,
            version = this.version + 1
        )
    }
}

data class ExtendedPassInfo(
    val passInfo: PassInfo,
    val testerTeststation: TesterTeststation
)
