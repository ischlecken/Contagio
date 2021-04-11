package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

enum class TestResultType {
    UNKNOWN, POSITIVE, NEGATIVE
}

enum class TestType {
    RAPIDTEST, PCRTEST, VACCINATION
}

enum class IssueStatus {
    CREATED, SIGNED, REFUSED
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
    val passId: String? = null,
    val validUntil: LocalDateTime? = null
)
