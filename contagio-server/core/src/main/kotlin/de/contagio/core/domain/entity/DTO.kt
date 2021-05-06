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
