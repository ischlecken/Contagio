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
    private val urlBuilder: UrlBuilder,
    private val passSigningInfo: PassSigningInfo
) {

    private val uidGenerator = UIDGenerator()
    private val authTokenGenerator = AuthTokenGenerator()

    fun execute(
        serialNumber: String,
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
        save: Boolean = false
    ): CreatePassResponse? {

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

        return searchTesterWithTeststation
            .execute(testerId)
            ?.let {
                val passInfoEnvelope = PassInfoEnvelope(
                    teamIdentifier = teamIdentifier,
                    passTypeIdentifier = passTypeIdentifier,
                    organisationName = organisationName,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.CREATED,
                    teststationId = it.teststation.id,
                    testerId = it.tester.id,
                    passInfoId = uidGenerator.generate(),
                )

                val passBuilderInfo = PassBuilderInfo(
                    passSigningInfo = passSigningInfo,
                    passImage = image,
                    passInfoEnvelope = passInfoEnvelope,
                    passInfo = passInfo,
                    teststation = it.teststation,
                    tester = it.tester
                )

                PassBuilder(passBuilderInfo, urlBuilder)
                    .build(authToken)
                    ?.let { pbr ->
                        val cpr = CreatePassResponse(
                            passInfoEnvelope = passInfoEnvelope,
                            passInfo = passInfo,
                            passImage = image,
                            authToken = authToken,
                            pkPass = pbr.pkpass,
                            pass = pbr.pass
                        )

                        if (save) {
                            save(cpr)
                        }

                        cpr
                    }
            }
    }

    private fun save(createPassResponse: CreatePassResponse) {
        with(createPassResponse) {
            saveRawEncryptedPayload.execute(passInfo.imageId, passImage, authToken)
            saveRawEncryptedPayload.execute(passInfo.passId, pass, authToken)
            saveEncryptedPayload.execute(passInfoEnvelope.passInfoId, passInfo, authToken)
            savePassInfoEnvelope.execute(passInfoEnvelope)
        }
    }
}
