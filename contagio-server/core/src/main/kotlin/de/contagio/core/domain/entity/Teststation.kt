package de.contagio.core.domain.entity

import de.contagio.core.domain.port.IUIDGenerator
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime


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

