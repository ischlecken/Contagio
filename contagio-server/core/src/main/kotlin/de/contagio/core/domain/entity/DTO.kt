package de.contagio.core.domain.entity

import de.brendamour.jpasskit.PKPass
import org.springframework.data.annotation.Id
import java.io.Serializable
import java.time.Instant


data class CreateTeststationDTO(
    var name: String = "",
    var zipcode: String = "",
    var city: String = "",
    var street: String = "",
    var hno: String = "",
) : Serializable


data class UpdateTeststationDTO(
    var name: String = "",
    var zipcode: String = "",
    var city: String = "",
    var street: String = "",
    var hno: String = "",
) : Serializable


data class CreateTesterDTO(
    val teststationId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNo: String = "",
    val email: String = ""
) : Serializable


data class UpdateTesterDTO(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNo: String = "",
    val email: String = ""
) : Serializable


@Suppress("ArrayInDataClass")
data class CreatePassResponse(
    val authToken: String,
    val passInfoEnvelope: PassInfoEnvelope,
    val passInfo: PassInfo,
    val passImage: ByteArray,
    val pkPass: PKPass,
    val pass: ByteArray
)

data class CreatePassRequestResponse(
    val serialNumber: String
)

@Suppress("ArrayInDataClass")
data class UpdatePassResponse(
    val authToken: String,
    val passInfoEnvelope: PassInfoEnvelope,
    val passInfo: PassInfo,
    val pkPass: PKPass,
    val pass: ByteArray
)

data class UpdatePassRequest(
    @Id val serialNumber: String,
    val testResult: TestResultType?,
    val issueStatus: IssueStatus?,
    val validUntil: Instant?,
    val created: Instant = Instant.now()
)

data class PassSerialNumberWithUpdated(
    val serialNumber: String,
    val updated: Instant
)
