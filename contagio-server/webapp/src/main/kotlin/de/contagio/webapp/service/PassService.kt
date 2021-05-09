package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.util.AuthTokenGenerator
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.CreatePassResponse
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant


private var logger = LoggerFactory.getLogger(PassService::class.java)

@Service
open class PassService(
    private val passInfoRepository: PassInfoRepository,
    private val passImageRepository: PassImageRepository,
    private val passRepository: PassRepository,
    private val passBuilderService: PassBuilderService,
    private val teststationRepository: TeststationRepository,
    private val testerRepository: TesterRepository,
    private val contagioProperties: ContagioProperties,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val registrationInfoRepository: RegistrationInfoRepository,
    private val pushNotificationService: PushNotificationService
) {

    private val uidGenerator = UIDGenerator()
    private val authTokenGenerator= AuthTokenGenerator()

    open fun delete(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            passInfo.passId?.let { passId ->
                passRepository.deleteById(passId)
            }

            passImageRepository.deleteById(passInfo.imageId)
            passInfoRepository.deleteById(passInfo.serialNumber)
        }
    }

    open fun issue(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            updateTestResult(serialnumber, passInfo.testResult, IssueStatus.ISSUED)

            notifyDevice(serialnumber)
        }
    }

    open fun expire(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            updateTestResult(serialnumber, passInfo.testResult, IssueStatus.EXPIRED)

            notifyDevice(serialnumber)
        }
    }

    open fun revoke(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            updateTestResult(serialnumber, passInfo.testResult, IssueStatus.REVOKED)

            notifyDevice(serialnumber)
        }
    }

    open fun negative(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->

            if (passInfo.issueStatus.isActive()) {
                updateTestResult(serialnumber, TestResultType.NEGATIVE, IssueStatus.ISSUED)

                notifyDevice(serialnumber)
            }
        }
    }

    open fun positive(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            if (passInfo.issueStatus.isActive()) {
                updateTestResult(serialnumber, TestResultType.POSITIVE, IssueStatus.ISSUED)

                notifyDevice(serialnumber)
            }
        }
    }

    open fun installed(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            passInfoRepository.save(
                passInfo.copy(
                    passInstallationStatus = PassInstallationStatus.INSTALLED,
                    passInstalled = Instant.now(),
                )
            )
        }
    }

    open fun removed(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            passInfoRepository.save(
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
    ): PassInfo? {
        var result: PassInfo? = null

        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
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
                    passInfoRepository.save(updatedPassInfo)

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
        backgroundColor: String
    ): CreatePassResponse {
        logger.debug("createPass(firstName=$firstName, lastName=$lastName, testResult=$testResult)")
        logger.debug("  image.size=${image.size}")

        val passInfo = PassInfo(
            serialNumber = uidGenerator.generate(),
            person = Person(firstName = firstName, lastName = lastName, phoneNo = phoneNo, email = email),
            imageId = uidGenerator.generate(),
            authToken = authTokenGenerator.generate(),
            testResult = testResult,
            testType = testType,
            passType = passType,
            issueStatus = IssueStatus.CREATED,
            teststationId = teststationId,
            testerId = testerId,
            description = contagioProperties.pass.description,
            logoText = contagioProperties.pass.logoText,
            labelColor = labelColor,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor
        )

        var result = CreatePassResponse(
            passInfo = passInfo,
            passImage = PassImage.build(image.bytes, image.contentType, passInfo)
        )

        val teststation = teststationRepository.findById(teststationId)
        val tester = testerRepository.findById(testerId)
        if (teststation.isPresent && tester.isPresent) {
            passBuilderService.buildPkPass(
                result.passImage,
                passInfo,
                teststation.get(),
                tester.get()
            )?.let {
                result = result.copy(pkPass = it)
            }
        }

        return result
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
    ): CreatePassResponse {

        var cpr = createPass(
            image,
            firstName, lastName,
            phoneNo, email,
            teststationId, testerId,
            testResult, testType,
            passType,
            labelColor, foregroundColor, backgroundColor
        )

        if (cpr.pkPass != null) {
            cpr = cpr.copy(
                passInfo = cpr.passInfo.copy(
                    passId = uidGenerator.generate(),
                    issueStatus = IssueStatus.ISSUED
                )
            )

            passRepository.save(Pass(id = cpr.passInfo.passId!!, data = cpr.pkPass!!))
            passImageRepository.save(cpr.passImage)
            passInfoRepository.save(cpr.passInfo)
        } else {
            cpr = cpr.copy(passInfo = cpr.passInfo.copy(issueStatus = IssueStatus.REFUSED))

            passImageRepository.save(cpr.passImage)
            passInfoRepository.save(cpr.passInfo)
        }

        return cpr
    }

    open fun updatePass(updatePassRequest: UpdatePassRequest): PassInfo? {
        val result = passInfoRepository.findById(updatePassRequest.serialNumber)

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

                pushNotificationService.sendPushNotificationAsync(deviceInfo.pushToken)
                    ?.thenApply {
                        logger.debug("  apns-id=${it.apnsId}")
                        logger.debug("  isAccepted=${it.isAccepted}")
                        logger.debug("  rejectionReason=${it.rejectionReason}")
                    }

            }
        }
    }
}
