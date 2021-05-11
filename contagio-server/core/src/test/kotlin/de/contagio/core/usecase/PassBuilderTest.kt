package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import org.apache.commons.io.IOUtils
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PassBuilderTest {

    @Test
    fun createPass_expectedValuesAndValid() {
        val authToken = Base64
            .getEncoder()
            .encodeToString(
                byteArrayOf(
                    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x00,
                    0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x10
                )
            )

        val passCoreInfo = PassCoreInfo(
            teamIdentifier = "teamid",
            passTypeIdentifier = "passTypeId",
            authenticationToken = "0123456789abcdef",
            organisationName = "bla org"
        )

        val passInfo = PassInfoEnvelope(
            serialNumber = "123",
            person = Person(firstName = "Hugo", lastName = "Schlecken"),
            passId = "123",
            imageId = "456",
            authToken = authToken,
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

        val iv = Base64
            .getEncoder()
            .encodeToString(
                byteArrayOf(
                    0x01, 0x02, 0x03, 0x04,
                    0x05, 0x06, 0x07, 0x08,
                    0x09, 0x0a, 0x0b, 0x0c,
                    0x0d, 0x0e, 0x0f, 0x00
                )
            )
        val encryptedImgData = Encryptor().execute(IOUtils.toByteArray(img), authToken, iv)

        val passBuilderInfo = PassBuilderInfo(
            passCoreInfo = passCoreInfo,
            passSigningInfo = PassSigningInfo(
                keystore = keystore,
                keystorePassword = "1234",
                appleWWDRCA = appleca,
            ),
            passImage = PassImage(
                id = "img",
                iv = iv,
                data = encryptedImgData,
                type = "image/png"
            ),
            passInfoEnvelope = passInfo,
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
