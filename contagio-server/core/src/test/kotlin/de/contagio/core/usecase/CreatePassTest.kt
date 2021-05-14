package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val logger: Logger = LoggerFactory.getLogger(CreatePassTest::class.java)

class CreatePassTest {

    @Test
    fun createPass_pkpassHasExpectedFieldsSet() {
        var passInfoId: String? = null

        val createPass = CreatePass(
            savePassInfoEnvelope = {

            },
            saveEncryptedPayload = { id, obj, key ->
                logger.debug("saveEncryptedPayload() id=$id key=$key")
                passInfoId = id

                object : IEncryptedPayload {
                    override fun getObject(key: String, cls: Class<*>): Any? {
                        TODO("Not yet implemented")
                    }

                    override fun get(key: String): ByteArray? {
                        TODO("Not yet implemented")
                    }
                }
            },
            saveRawEncryptedPayload = { id, obj, key ->
                logger.debug("saveRawEncryptedPayload() id=$id key=$key")

                object : IEncryptedPayload {
                    override fun getObject(key: String, cls: Class<*>): Any? {
                        TODO("Not yet implemented")
                    }

                    override fun get(key: String): ByteArray? {
                        TODO("Not yet implemented")
                    }
                }
            },
            searchTesterWithTeststation = SearchTesterWithTeststation(
                findTester = {
                    Tester(
                        id = "1",
                        teststationId = "1",
                        person = Person(firstName = "vorname tester", lastName = "nachname tester")
                    )
                },
                findTeststation = {
                    Teststation(
                        id = "1",
                        name = "teststation name",
                        address = Address(city = "bla city", zipcode = "00000", street = "blastreet")
                    )
                }
            ),
            urlBuilder = UrlBuilder("https://bla.de")
        )

        val img = CreatePassTest::class.java.getResourceAsStream("/testimg.png")
        val keystore = CreatePassTest::class.java.getResourceAsStream("/certs/pass.p12")
        val appleca = CreatePassTest::class.java.getResourceAsStream("/certs/AppleWWDRCA.cer")

        val passSigningInfo = PassSigningInfo(
            keystore = keystore!!,
            keystorePassword = "1234",
            appleWWDRCA = appleca!!,
        )

        val cpr = createPass.execute(
            teamIdentifier = "xx4V27BGKSLA",
            passTypeIdentifier = "pass.de.contagio.til",
            organisationName = "Contagio - TIL",
            description = "TIL",
            logoText = "LOGO_TESTTYPE",
            image = IOUtils.toByteArray(img),
            firstName = "Hugo",
            lastName = "Schlecken",
            phoneNo = "089454545",
            email = "hugo@schlecken",
            testerId = "1",
            testResult = TestResultType.UNKNOWN,
            testType = TestType.RAPIDTEST,
            passType = PassType.COUPON,
            labelColor = "rgb(5, 175, 190)",
            foregroundColor = "rgb(255, 255, 255)",
            backgroundColor = "rgb(208, 38, 0)",
            passSigningInfo = passSigningInfo,
            save = true
        )

        assertNotNull(cpr)
        assertEquals(TestType.RAPIDTEST, cpr.passInfo.testType)
        assertEquals("Hugo Schlecken", cpr.passInfo.person.fullName)

        assertEquals("LOGO_TESTTYPE", cpr.pkPass.logoText)

        assertEquals("testResult", cpr.pkPass.coupon.primaryFields[0].key)
        assertEquals("TESTRESULT", cpr.pkPass.coupon.primaryFields[0].label)
        assertEquals("TESTRESULT_UNKNOWN", cpr.pkPass.coupon.primaryFields[0].value)

        assertEquals("teststation", cpr.pkPass.coupon.auxiliaryFields[0].key)
        assertEquals("TESTSTATION", cpr.pkPass.coupon.auxiliaryFields[0].label)
        assertEquals("teststation name", cpr.pkPass.coupon.auxiliaryFields[0].value)

        assertEquals("testType", cpr.pkPass.coupon.headerFields[0].key)
        assertEquals("TESTTYPE_RAPIDTEST", cpr.pkPass.coupon.headerFields[0].value)

        assertEquals(cpr.passInfoEnvelope.serialNumber, cpr.pkPass.serialNumber)
        assertEquals(passInfoId, cpr.passInfoEnvelope.passInfoId)
    }
}
