package de.contagio.core.usecase

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.ISaveEncryptedPayload
import de.contagio.core.domain.port.ISavePassInfoEnvelope
import de.contagio.core.domain.port.ISaveRawEncryptedPayload
import de.contagio.core.util.AuthTokenGenerator
import de.contagio.core.util.UIDGenerator

class CreatePass(
    private val savePassInfoEnvelope: ISavePassInfoEnvelope,
    private val saveEncryptedPayload: ISaveEncryptedPayload,
    private val saveRawEncryptedPayload: ISaveRawEncryptedPayload,
    private val searchTesterWithTeststation: SearchTesterWithTeststation,
    private val urlBuilder: UrlBuilder
) {

    private val uidGenerator = UIDGenerator()
    private val authTokenGenerator = AuthTokenGenerator()

    fun execute(
        teamIdentifier: String,
        passTypeIdentifier: String,
        organisationName: String,
        description: String,
        logoText: String,
        image: ByteArray,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String?,
        testerId: String,
        testResult: TestResultType,
        testType: TestType,
        passType: PassType,
        labelColor: String,
        foregroundColor: String,
        backgroundColor: String,
        passSigningInfo: PassSigningInfo
    ): CreatePassResponse? {

        var result: CreatePassResponse? = null
        val authToken = authTokenGenerator.generate()

        val passInfo = PassInfo(
            person = Person(firstName = firstName, lastName = lastName, phoneNo = phoneNo, email = email),
            imageId = uidGenerator.generate(),
            passId = uidGenerator.generate(),
            testResult = testResult,
            testType = testType,
            passType = passType,
            description = description,
            logoText = logoText,
            labelColor = labelColor,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor
        )

        searchTesterWithTeststation.execute(testerId)?.apply {
            val passInfoEnvelope = PassInfoEnvelope(
                teamIdentifier = teamIdentifier,
                passTypeIdentifier = passTypeIdentifier,
                organisationName = organisationName,
                serialNumber = uidGenerator.generate(),
                issueStatus = IssueStatus.CREATED,
                teststationId = teststation.id,
                testerId = tester.id,
                passInfoId = uidGenerator.generate(),
            )

            val passBuilderInfo = PassBuilderInfo(
                passSigningInfo = passSigningInfo,
                passImage = image,
                passInfoEnvelope = passInfoEnvelope,
                passInfo = passInfo,
                teststation = teststation,
                tester = tester
            )

            PassBuilder(passBuilderInfo, urlBuilder)
                .build(authToken)
                ?.apply {
                    result = CreatePassResponse(
                        passInfoEnvelope = passInfoEnvelope,
                        passInfo = passInfo,
                        passImage = image,
                        authToken = authToken,
                        pkPass = pass
                    )
                }
        }

        return result
    }

    fun save(createPassResponse: CreatePassResponse) {
        with(createPassResponse) {
            saveRawEncryptedPayload.execute(passInfo.imageId, passImage, authToken)
            saveRawEncryptedPayload.execute(passInfo.passId, pkPass, authToken)
            saveEncryptedPayload.execute(passInfoEnvelope.passInfoId, passInfo, authToken)
            savePassInfoEnvelope.execute(passInfoEnvelope)
        }
    }
}
