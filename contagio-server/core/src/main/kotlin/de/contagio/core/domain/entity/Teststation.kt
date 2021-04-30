package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.Instant


data class Teststation(
    @Id val id: String,
    val name: String,
    val address: Address,
    val created: Instant = Instant.now()
)


data class Tester(
    @Id val id: String,
    val teststationId: String,
    val person: Person,
    val created: Instant = Instant.now()
)


data class TesterTeststation(
    val tester: Tester,
    val teststation: Teststation
)


data class TesterInfo(
    val displayInfo: String,
    val testerTeststation: TesterTeststation,
)
