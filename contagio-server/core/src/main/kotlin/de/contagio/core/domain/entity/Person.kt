package de.contagio.core.domain.entity

import de.contagio.core.domain.port.IUIDGenerator
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
    val fullAddress: String
        get() = "$zipcode $city, $street $hno"
}

data class Person(
    val firstName: String,
    val lastName: String,
    val phoneNo: String? = null,
    val email: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
}

