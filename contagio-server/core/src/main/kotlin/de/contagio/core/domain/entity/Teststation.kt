package de.contagio.core.domain.entity

import org.springframework.data.annotation.Id
import java.time.LocalDateTime


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


data class TesterTeststation(
    val tester: Tester,
    val teststation: Teststation
)


data class TesterInfo(
    val displayInfo: String,
    val testerTeststation: TesterTeststation,
)
