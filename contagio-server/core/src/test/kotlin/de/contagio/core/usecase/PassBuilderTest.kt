package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import org.apache.commons.io.IOUtils
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

        val img = PassBuilderTest::class.java.getResourceAsStream("/testimg.png")
        val keystore = PassBuilderTest::class.java.getResourceAsStream("/certs/pass.p12")
        val appleca = PassBuilderTest::class.java.getResourceAsStream("/certs/AppleWWDRCA.cer")

        val passBuilderInfo = PassBuilderInfo(
            passCoreInfo = passCoreInfo,
            passSigningInfo = PassSigningInfo(
                keystore = keystore,
                keystorePassword = "1234",
                appleWWDRCA = appleca,
            ),
            passImage = PassImage(
                id = "img",
                data = IOUtils.toByteArray(img),
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

        val urlBuilder = UrlBuilder("http://bla.de")

        val cpr = PassBuilder(passBuilderInfo, urlBuilder).build()

        assertEquals("123", cpr.pkpass.serialNumber)
        assertEquals("teamid", cpr.pkpass.teamIdentifier)
        assertEquals("passTypeId", cpr.pkpass.passTypeIdentifier)
        assertEquals("0123456789abcdef", cpr.pkpass.authenticationToken)
        assertEquals(true, cpr.pkpass.isSharingProhibited)

        val validationErrors = cpr.pkpass.validationErrors
        assertEquals(0, validationErrors.size)
    }
}
