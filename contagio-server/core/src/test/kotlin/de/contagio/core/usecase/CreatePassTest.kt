package de.contagio.core.usecase

import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.entity.Person
import de.contagio.core.domain.entity.TestResultType
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
                teststationId = "1",
                testerId = "1"
            ),
            PassImage(
                id = "adfa",
                data = ByteArray(10),
                type = "image/bla"
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
