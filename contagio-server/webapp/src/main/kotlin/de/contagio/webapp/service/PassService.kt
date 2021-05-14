package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.*
import de.contagio.core.usecase.*
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.DeviceInfoRepository
import de.contagio.webapp.repository.mongodb.PassInfoEnvelopeRepository
import de.contagio.webapp.repository.mongodb.RegistrationInfoRepository
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
    private val contagioProperties: ContagioProperties,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val registrationInfoRepository: RegistrationInfoRepository,
    private val pushNotificationService: PushNotificationService,
    private val savePassInfoEnvelope: ISavePassInfoEnvelope,
    private val saveEncryptedPayload: ISaveEncryptedPayload,
    private val saveRawEncryptedPayload: ISaveRawEncryptedPayload,
    private val searchTesterWithTeststation: SearchTesterWithTeststation,
    private val urlBuilder: UrlBuilder,
    private val getEncryptionKey: IGetEncryptionKey,
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val findEncryptedPayload: IFindEncryptedPayload
) {

    @Value("classpath:certs/pass.p12")
    private lateinit var passKeystore: Resource

    @Value("classpath:certs/AppleWWDRCA.cer")
    private lateinit var appleWWDRCA: Resource

    @Value("classpath:certs/contagio-sign.p12")
    private lateinit var contagioSignKeystore: Resource


    /**
     * TODO: queue deletion of image and pass until authToken is known
     */
    open fun delete(serialnumber: String) {
        passInfoEnvelopeRepository.findById(serialnumber).ifPresent { passInfo ->
            passInfoEnvelopeRepository.deleteById(passInfo.serialNumber)
        }
    }

    open fun issue(serialnumber: String) {
        createUpdatePass()
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.ISSUED,
                testResult = null,
                validUntil = null,
                passSigningInfo = createPassSigningInfo()
            )?.also {
                notifyDevice(serialnumber)
            }
    }

    open fun expire(serialnumber: String) {
        createUpdatePass()
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.EXPIRED,
                testResult = null,
                validUntil = null,
                passSigningInfo = createPassSigningInfo()
            )?.also {
                notifyDevice(serialnumber)
            }
    }

    open fun revoke(serialnumber: String) {
        createUpdatePass()
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.REVOKED,
                testResult = null,
                validUntil = null,
                passSigningInfo = createPassSigningInfo()
            )?.also {
                notifyDevice(serialnumber)
            }
    }

    open fun negative(serialnumber: String) {
        createUpdatePass()
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.ISSUED,
                testResult = TestResultType.NEGATIVE,
                validUntil = null,
                passSigningInfo = createPassSigningInfo()
            )?.also {
                notifyDevice(serialnumber)
            }
    }

    open fun positive(serialnumber: String) {
        createUpdatePass()
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.ISSUED,
                testResult = TestResultType.POSITIVE,
                validUntil = null,
                passSigningInfo = createPassSigningInfo()
            )?.also {
                notifyDevice(serialnumber)
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
            passSigningInfo = createPassSigningInfo(),
            save = save
        )
    }

    open fun updatePass(updatePassRequest: UpdatePassRequest) =
        createUpdatePass()
            .execute(
                serialNumber = updatePassRequest.serialNumber,
                issueStatus = IssueStatus.ISSUED,
                testResult = updatePassRequest.testResult,
                validUntil = updatePassRequest.validUntil,
                passSigningInfo = createPassSigningInfo()
            )?.let {
                notifyDevice(updatePassRequest.serialNumber)

                it.passInfoEnvelope
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

    open fun sign(data: ByteArray) =
        SignatureBuilder(
            contagioSignKeystore.inputStream,
            contagioProperties.sign.password,
            contagioProperties.sign.keyname
        ).execute(data)

    private fun createUpdatePass() = UpdatePass(
        findPassInfoEnvelope = findPassInfoEnvelope,
        findEncryptedPayload = findEncryptedPayload,
        savePassInfoEnvelope = savePassInfoEnvelope,
        saveEncryptedPayload = saveEncryptedPayload,
        saveRawEncryptedPayload = saveRawEncryptedPayload,
        searchTesterWithTeststation = searchTesterWithTeststation,
        getEncryptionKey = getEncryptionKey,
        urlBuilder = urlBuilder
    )

    private fun createPassSigningInfo() = PassSigningInfo(
        keystore = passKeystore.inputStream,
        keystorePassword = contagioProperties.pass.keystorePassword,
        appleWWDRCA = appleWWDRCA.inputStream
    )
}
