package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class PassBuilderTest {

    @Test
    fun createPass_expectedValuesAndValid() {
        val passCoreInfo = PassCoreInfo(
            teamIdentifier = "teamid",
            passTypeIdentifier = "passTypeId",
            authenticationToken = "0123456789abcdef",
            baseUrl = "http://bla.de",
            organisationName = "bla org"
        )

        val passInfo = PassInfo(
            serialNumber = "123",
            person = Person(firstName = "Hugo", lastName = "Schlecken"),
            passId = "123",
            imageId = "456",
            authToken = "abc",
            testResult = TestResultType.NEGATIVE,
            testType = TestType.RAPIDTEST,
            passType = PassType.GENERIC,
            issueStatus = IssueStatus.CREATED,
            teststationId = "1",
            testerId = "1",
            description = "bla",
            logoText = "fasel",
            labelColor = "rgb(5, 175, 190)",
            foregroundColor = "rgb(255, 255, 255)",
            backgroundColor = "rgb(208, 38, 0)"
        )

        val passBuilderInfo = PassBuilderInfo(
            passCoreInfo = passCoreInfo,
            passSigningInfo = PassSigningInfo(
                keystore = ByteArrayInputStream("bla".toByteArray()),
                keystorePassword = "bla",
                appleWWDRCA = ByteArrayInputStream("fasel".toByteArray()),
            ),
            passImage = PassImage(
                id = "img",
                data = byteArrayOf(0x1f, 0x1e),
                type = "image/png"
            ),
            passInfo = passInfo,
            teststation = Teststation(
                id = "1",
                "Teststation",
                address = Address(city = "Blacity", zipcode = "1234")
            ),
            tester = Tester(
                id = "1",
                teststationId = "1",
                person = Person(firstName = "Ingo", lastName = "Tester1"),
            )
        )

        val cpr = PassBuilder(passBuilderInfo).build()

        assertEquals("123", cpr.pkpass.serialNumber)
        assertEquals("teamid", cpr.pkpass.teamIdentifier)
        assertEquals("passTypeId", cpr.pkpass.passTypeIdentifier)
        assertEquals("0123456789abcdef", cpr.pkpass.authenticationToken)
        assertEquals(true, cpr.pkpass.isSharingProhibited)

        val validationErrors = cpr.pkpass.validationErrors
        assertEquals(0, validationErrors.size)
    }
}
