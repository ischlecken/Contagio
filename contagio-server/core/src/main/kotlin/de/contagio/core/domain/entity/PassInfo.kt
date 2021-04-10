package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

enum class TestResultType(val display: String) {
    UNKNOWN("unbekannt"), POSITIVE("positiv"), NEGATIVE("negativ")
}

data class PassInfo(
    @Id val serialNumber: String,
    val userId: String,
    val imageId: String,
    val passId: String,
    val authToken: String,
    val testResult: TestResultType,
    val created: LocalDateTime = LocalDateTime.now(),
    val validUntil: LocalDateTime = LocalDateTime.now().plusHours(12)
)

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
