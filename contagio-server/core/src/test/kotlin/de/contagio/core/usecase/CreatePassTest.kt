package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CreatePassTest {

    @Test
    fun createPass_expectedValuesAndValid() {
        val createPass =
            CreatePass(
                teamIdentifier = "teamid",
                passTypeIdentifier = "passTypeId",
                authenticationToken = "0123456789abcdef",
                baseUrl = "http://bla.de"
            )

        val person = Person(firstName = "Hugo", lastName = "Schlecken")
        val pass = createPass.build(
            PassInfo(
                serialNumber = "123",
                person = person,
                passId = "123",
                imageId = "456",
                authToken = "abc",
                testResult = TestResultType.NEGATIVE,
                testType = TestType.RAPIDTEST,
                issueStatus = IssueStatus.CREATED,
                teststationId = "1",
                testerId = "1"
            ),
            Teststation(id = "1","Teststation",address = Address(city="Blacity",zipcode = "1234")),
            CreatePassParameter(
                organisationName = "hugo",
                description = "bla",
                logoText = "fasel"
            )
        )

        assertEquals("123", pass.serialNumber)
        assertEquals("teamid", pass.teamIdentifier)
        assertEquals("passTypeId", pass.passTypeIdentifier)
        assertEquals("0123456789abcdef", pass.authenticationToken)

        val validationErrors = pass.validationErrors
        assertEquals(0, validationErrors.size)
    }
}
