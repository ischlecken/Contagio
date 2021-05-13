package de.contagio.core.domain.entity

import java.io.Serializable


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
    val pkPass: ByteArray
)
