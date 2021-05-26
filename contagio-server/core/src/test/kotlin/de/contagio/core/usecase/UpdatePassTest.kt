package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import de.contagio.core.util.AuthTokenGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UpdatePassTest {

    @Test
    fun updatePass_isExpected() {
        val authToken = AuthTokenGenerator().generate()
        var updatedPassInfo: PassInfo? = null

        val updatePass = UpdatePass(
            findPassInfoEnvelope = {
                PassInfoEnvelope(
                    serialNumber = it,
                    teamIdentifier = "123",
                    passTypeIdentifier = "123",
                    organisationName = "123",
                    passInfoId = "passInfoId123",
                    imageId = "imageId1",
                    passId = "passId1",
                    teststationId = "1",
                    testerId = "1",
                    issueStatus = IssueStatus.ISSUED
                )
            },
            findEncryptedPayload = {
                object : IEncryptedPayload {
                    override fun getObject(key: String?, cls: Class<*>): Any? {
                        return when (it) {
                            "passInfoId123" -> PassInfo(
                                person = Person(firstName = "helge", lastName = "schneider"),
                                testResult = TestResultType.UNKNOWN,
                                testType = TestType.RAPIDTEST,
                                passType = PassType.COUPON,
                                description = "bla",
                                logoText = "bla",
                                labelColor = "rgb(255, 255, 255)",
                                foregroundColor = "rgb(255, 255, 255)",
                                backgroundColor = "rgb(255, 255, 255)",
                            )
                            else -> null
                        }
                    }

                    override fun get(key: String?): ByteArray? {
                        return when (it) {
                            "imageId1" -> CreatePassTest.passImage()
                            else -> null
                        }
                    }

                }
            },
            savePassInfoEnvelope = {

            },
            saveEncryptedPayload = { id, obj, _ ->
                if (id == "passInfoId123")
                    updatedPassInfo = obj as PassInfo

                object : IEncryptedPayload {
                    override fun getObject(key: String?, cls: Class<*>): Any? {
                        TODO("Not yet implemented")
                    }

                    override fun get(key: String?): ByteArray? {
                        TODO("Not yet implemented")
                    }
                }
            },
            saveRawEncryptedPayload = { _, _, _ ->
                object : IEncryptedPayload {
                    override fun getObject(key: String?, cls: Class<*>): Any? {
                        TODO("Not yet implemented")
                    }

                    override fun get(key: String?): ByteArray? {
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
            urlBuilder = UrlBuilder("https://blafasel.de"),
            passSigningInfo = CreatePassTest.passSigningInfo()
        )

        val result = updatePass.execute(
            authToken = authToken,
            serialNumber = "123456",
            testResult = TestResultType.NEGATIVE,
            validUntil = null,
            issueStatus = null
        )

        assertNotNull(result)
        assertEquals("123456", result.passInfoEnvelope.serialNumber)
        assertEquals("passInfoId123", result.passInfoEnvelope.passInfoId)
        assertEquals(TestResultType.NEGATIVE, updatedPassInfo?.testResult)
    }
}
