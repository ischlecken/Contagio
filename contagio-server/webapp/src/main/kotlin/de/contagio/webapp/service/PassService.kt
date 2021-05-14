package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.ISaveEncryptedPayload
import de.contagio.core.domain.port.ISavePassInfoEnvelope
import de.contagio.core.domain.port.ISaveRawEncryptedPayload
import de.contagio.core.usecase.CreatePass
import de.contagio.core.usecase.SearchTesterWithTeststation
import de.contagio.core.usecase.SignatureBuilder
import de.contagio.core.usecase.UrlBuilder
import de.contagio.core.util.AuthTokenGenerator
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

private var logger = LoggerFactory.getLogger(PassService::class.java)

@Service
open class PassService(
    private val passInfoEnvelopeRepository: PassInfoEnvelopeRepository,
    private val teststationRepository: TeststationRepository,
    private val testerRepository: TesterRepository,
    private val contagioProperties: ContagioProperties,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val registrationInfoRepository: RegistrationInfoRepository,
    private val pushNotificationService: PushNotificationService,
    private val savePassInfoEnvelope: ISavePassInfoEnvelope,
    private val saveEncryptedPayload: ISaveEncryptedPayload,
    private val saveRawEncryptedPayload: ISaveRawEncryptedPayload,
    private val searchTesterWithTeststation: SearchTesterWithTeststation,
    private val urlBuilder: UrlBuilder
) {

    @Value("classpath:certs/pass.p12")
    private lateinit var passKeystore: Resource

    @Value("classpath:certs/AppleWWDRCA.cer")
    private lateinit var appleWWDRCA: Resource

    @Value("classpath:certs/contagio-sign.p12")
    private lateinit var contagioSignKeystore: Resource

    private val uidGenerator = UIDGenerator()
    private val authTokenGenerator = AuthTokenGenerator()

    /**
     * TODO: delete image and pass if authToken present
     */
    open fun delete(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            passInfoEnvelopeRepository.deleteById(passInfo.serialNumber)
        }
    }

    open fun issue(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            updateTestResult(serialnumber, passInfo.testResult, IssueStatus.ISSUED)

            notifyDevice(serialnumber)
        }
    }

    open fun expire(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            updateTestResult(serialnumber, passInfo.testResult, IssueStatus.EXPIRED)

            notifyDevice(serialnumber)
        }
    }

    open fun revoke(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            updateTestResult(serialnumber, passInfo.testResult, IssueStatus.REVOKED)

            notifyDevice(serialnumber)
        }
    }

    open fun negative(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->

            if (passInfo.issueStatus.isActive()) {
                updateTestResult(serialnumber, TestResultType.NEGATIVE, IssueStatus.ISSUED)

                notifyDevice(serialnumber)
            }
        }
    }

    open fun positive(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            if (passInfo.issueStatus.isActive()) {
                updateTestResult(serialnumber, TestResultType.POSITIVE, IssueStatus.ISSUED)

                notifyDevice(serialnumber)
            }
        }
    }

    open fun installed(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            passInfoEnvelopeRepository.save(
                passInfo.copy(
                    passInstallationStatus = PassInstallationStatus.INSTALLED,
                    passInstalled = Instant.now(),
                )
            )
        }
    }

    open fun removed(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            passInfoEnvelopeRepository.save(
                passInfo.copy(
                    passInstallationStatus = PassInstallationStatus.REMOVED,
                    passInstalled = Instant.now(),
                    passRemoved = Instant.now()
                )
            )
        }
    }

    private fun updateTestResult(
        serialnumber: String,
        testResult: TestResultType,
        issueStatus: IssueStatus,
        validUntil: Instant? = null,
    ): PassInfoEnvelope? {
        var result: PassInfoEnvelope? = null

        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            val updatedPassInfo = passInfo.update(testResult, issueStatus, validUntil)
            val pass = passRepository.findById(passInfo.passId!!)
            val passImage = passImageRepository.findById(passInfo.imageId)
            val teststation = teststationRepository.findById(passInfo.teststationId)
            val tester = testerRepository.findById(passInfo.testerId)
            if (teststation.isPresent && tester.isPresent && passImage.isPresent && pass.isPresent) {
                passBuilderService.buildPkPass(
                    passImage.get(),
                    updatedPassInfo,
                    teststation.get(),
                    tester.get()
                )?.let {
                    passRepository.save(pass.get().copy(data = it))
                    passInfoEnvelopeRepository.save(updatedPassInfo)

                    result = updatedPassInfo
                }
            }
        }

        return result
    }

    open fun createPass(
        image: MultipartFile,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String?,
        teststationId: String,
        testerId: String,
        testResult: TestResultType,
        testType: TestType,
        passType: PassType,
        labelColor: String,
        foregroundColor: String,
        backgroundColor: String,
        save: Boolean = false
    ): CreatePassResponse? {
        logger.debug("createPass(firstName=$firstName, lastName=$lastName, testResult=$testResult)")
        logger.debug("  image.size=${image.size}")

        val passSigningInfo = PassSigningInfo(
            keystore = passKeystore.inputStream,
            keystorePassword = contagioProperties.pass.keystorePassword,
            appleWWDRCA = appleWWDRCA.inputStream
        )

        val createPass = CreatePass(
            savePassInfoEnvelope,
            saveEncryptedPayload,
            saveRawEncryptedPayload,
            searchTesterWithTeststation,
            urlBuilder
        )

        return createPass.execute(
            teamIdentifier = contagioProperties.pass.teamIdentifier,
            passTypeIdentifier = contagioProperties.pass.passTypeId,
            organisationName = contagioProperties.pass.organisationName,
            description = contagioProperties.pass.description,
            logoText = contagioProperties.pass.logoText,
            image = image.bytes,
            firstName = firstName,
            lastName = lastName,
            phoneNo = phoneNo,
            email = email,
            testerId = testerId,
            testResult = testResult,
            testType = testType,
            passType = passType,
            labelColor = labelColor,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            passSigningInfo = passSigningInfo,
            save = save
        )
    }

    open fun createPassAndSave(
        image: MultipartFile,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String?,
        teststationId: String,
        testerId: String,
        testResult: TestResultType,
        testType: TestType,
        passType: PassType = PassType.COUPON,
        labelColor: String = contagioProperties.pass.labelColor,
        foregroundColor: String = contagioProperties.pass.foregroundColor,
        backgroundColor: String = contagioProperties.pass.backgroundColor
    ): CreatePassResponse? {

        return createPass(
            image,
            firstName, lastName,
            phoneNo, email,
            teststationId, testerId,
            testResult, testType,
            passType,
            labelColor, foregroundColor, backgroundColor,
            true
        )
    }

    open fun updatePass(updatePassRequest: UpdatePassRequest): PassInfoEnvelope? {
        val result = passInfoEnvelopeRepository.findById(updatePassRequest.serialNumber)

        if (result.isPresent && updatePassRequest.testResult != null) {
            val passInfo = updateTestResult(
                updatePassRequest.serialNumber,
                updatePassRequest.testResult,
                IssueStatus.ISSUED,
                updatePassRequest.validUntil
            )

            if (passInfo != null)
                notifyDevice(updatePassRequest.serialNumber)

            return passInfo
        }

        return null
    }

    open fun notifyDevice(serialnumber: String) {
        registrationInfoRepository.findBySerialNumber(serialnumber).forEach { registrationInfo ->
            deviceInfoRepository.findById(registrationInfo.deviceLibraryIdentifier).ifPresent { deviceInfo ->
                logger.debug("found push token ${deviceInfo.pushToken} for serialnumber $serialnumber...")

                pushNotificationService
                    .sendPushNotificationAsync(deviceInfo.pushToken)
                    ?.thenApply {
                        logger.debug("  apns-id=${it.apnsId}")
                        logger.debug("  isAccepted=${it.isAccepted}")
                        logger.debug("  rejectionReason=${it.rejectionReason}")
                    }

            }
        }
    }


    open fun sign(data: ByteArray) = SignatureBuilder(
        contagioSignKeystore.inputStream,
        contagioProperties.sign.password,
        contagioProperties.sign.keyname
    ).execute(data)
}
