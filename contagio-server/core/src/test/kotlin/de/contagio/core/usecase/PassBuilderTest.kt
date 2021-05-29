package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.IdType
import org.apache.commons.io.IOUtils
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PassBuilderTest {

    private val key = Base64
        .getEncoder()
        .encodeToString(
            byteArrayOf(
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x00,
                0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x10
            )
        )

    private val getEncryptionKey = IGetEncryptionKey { _, id ->
        if (id == "123")
            key
        else
            null
    }

    private val passInfoTemplate = PassInfo(
        person = Person(firstName = "Hugo", lastName = "Schlecken"),
        testResult = TestResultType.NEGATIVE,
        testType = TestType.RAPIDTEST,
        passType = PassType.GENERIC,
        description = "bla",
        logoText = "fasel",
        labelColor = "rgb(5, 175, 190)",
        foregroundColor = "rgb(255, 255, 255)",
        backgroundColor = "rgb(208, 38, 0)"
    )

    private val passInfoEnvelopeTemplate = PassInfoEnvelope(
        serialNumber = "123",
        issueStatus = IssueStatus.CREATED,
        teststationId = "1",
        testerId = "1",
        passInfoId = "4242",
        passId = "123123",
        imageId = "456",
        teamIdentifier = "teamid",
        passTypeIdentifier = "passTypeId",
        organisationName = "bla org"
    )


    @Test
    fun createPass_expectedValuesAndValid() {
        val img = PassBuilderTest::class.java.getResourceAsStream("/testimg.png")
        val keystore = IOUtils.toByteArray(PassBuilderTest::class.java.getResourceAsStream("/certs/pass.p12"))
        val appleca = IOUtils.toByteArray(PassBuilderTest::class.java.getResourceAsStream("/certs/AppleWWDRCA.cer"))

        val passBuilderInfo = PassBuilderInfo(
            passSigningInfo = PassSigningInfo(
                keystore = keystore!!,
                keystorePassword = "1234",
                appleWWDRCA = appleca!!,
            ),
            passImage = IOUtils.toByteArray(img),
            passInfoEnvelope = passInfoEnvelopeTemplate,
            passInfo = passInfoTemplate,
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

        val urlBuilder = UrlBuilder("https://bla.de")
        val cpr = PassBuilder(passBuilderInfo, urlBuilder).build(key)

        assertEquals("123", cpr?.pkpass?.serialNumber)
        assertEquals("teamid", cpr?.pkpass?.teamIdentifier)
        assertEquals("passTypeId", cpr?.pkpass?.passTypeIdentifier)
        assertEquals(getEncryptionKey.execute(IdType.SERIALNUMBER,"123"), cpr?.pkpass?.authenticationToken)
        assertEquals(true, cpr?.pkpass?.isSharingProhibited)

        val validationErrors = cpr?.pkpass?.validationErrors
        assertEquals(0, validationErrors?.size)
    }

}
