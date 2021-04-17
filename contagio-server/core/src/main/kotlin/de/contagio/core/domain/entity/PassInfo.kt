package de.contagio.core.domain.entity

import de.contagio.core.domain.port.IUIDGenerator
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

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
    CREATED, SIGNED, EXPIRED, REVOKED, REFUSED, PENDING, FAILED, UNKNOWN
}


data class GeoPosition(
    val latitude: String,
    val longitude: String
)

data class Address(
    val city: String,
    val zipcode: String,
    val street: String? = null,
    val hno: String? = null,
    val position: GeoPosition? = null,
)

data class Person(
    val firstName: String,
    val lastName: String,
    val phoneNo: String? = null,
    val email: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
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


data class Teststation(
    @Id val id: String,
    val name: String,
    val address: Address,
    val created: LocalDateTime = LocalDateTime.now()
)


data class Tester(
    @Id val id: String,
    val teststationId: String,
    val person: Person,
    val created: LocalDateTime = LocalDateTime.now()
)


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
    val created: LocalDateTime = LocalDateTime.now(),
    val modified: LocalDateTime? = null,
    val passId: String? = null,
    val validUntil: LocalDateTime? = null,
    val version: Int = 0
) {
    companion object {
        fun build(
            uidGenerator: IUIDGenerator,
            firstName: String,
            lastName: String,
            phoneNo: String,
            email: String?,
            teststationId: String,
            testerId: String,
            testResult: TestResultType,
            testType: TestType
        ): PassInfo {
            return PassInfo(
                serialNumber = uidGenerator.generate(),
                person = Person(firstName = firstName, lastName = lastName, phoneNo = phoneNo, email = email),
                imageId = uidGenerator.generate(),
                authToken = uidGenerator.generate(),
                testResult = testResult,
                testType = testType,
                issueStatus = IssueStatus.CREATED,
                testerId = testerId,
                teststationId = teststationId
            )
        }
    }

    val updated: LocalDateTime get() = modified ?: created
}
