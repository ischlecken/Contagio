package de.contagio.core.domain.entity

import com.fasterxml.jackson.annotation.JsonIgnore

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
) {
    @get:JsonIgnore
    val fullAddress: String
        get() = "$zipcode $city, $street $hno"
}

data class Person(
    val firstName: String,
    val lastName: String,
    val phoneNo: String? = null,
    val email: String? = null
) {
    @get:JsonIgnore
    val fullName: String
        get() = "$firstName $lastName"
}

