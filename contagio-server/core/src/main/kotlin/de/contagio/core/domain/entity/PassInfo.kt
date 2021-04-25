package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
    CREATED, ISSUED, EXPIRED, REVOKED, REFUSED
}


enum class PassInstallationStatus {
    PENDING, INSTALLED, REMOVED
}

data class PassImage(
    @Id val id: String,
    val data: ByteArray,
    val type: String,
    val created: LocalDateTime = LocalDateTime.now()
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
    val created: LocalDateTime = LocalDateTime.now()
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
    val created: LocalDateTime = LocalDateTime.now(),
    val modified: LocalDateTime? = null,
    val validUntil: LocalDateTime? = null,
    val passInstalled: LocalDateTime? = null,
    val passRemoved: LocalDateTime? = null

) {
    val updated: LocalDateTime get() = modified ?: created
    val updatedUTC: ZonedDateTime get() = ZonedDateTime.ofInstant(updated, ZoneOffset.of("+02:00"), ZoneId.of("GMT"))
}

