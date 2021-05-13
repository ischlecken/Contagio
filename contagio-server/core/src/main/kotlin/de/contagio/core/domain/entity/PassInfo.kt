package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.Instant

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


data class PassInfo(
    val person: Person,
    val testResult: TestResultType,
    val testType: TestType,
    val passType: PassType,
    val description: String,
    val logoText: String,
    val labelColor: String,
    val foregroundColor: String,
    val backgroundColor: String,
    val imageId: String,
    val passId: String
)

data class PassInfoEnvelope(
    @Id val serialNumber: String,
    val teamIdentifier: String,
    val passTypeIdentifier: String,
    val organisationName: String,
    val passInfoId: String,
    val teststationId: String,
    val testerId: String,
    val issueStatus: IssueStatus,
    val version: Int = 0,
    val updated: Instant = Instant.now(),
    val validUntil: Instant? = null,
    val passInstallationStatus: PassInstallationStatus = PassInstallationStatus.PENDING,
    val passInstalled: Instant? = null,
    val passRemoved: Instant? = null
)

data class ExtendedPassInfo(
    val passInfoEnvelope: PassInfoEnvelope,
    val testerTeststation: TesterTeststation,
    val passInfo: PassInfo? = null
)
