package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.IFindPassInfoEnvelope
import de.contagio.core.domain.port.IdType
import de.contagio.core.usecase.*
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

private var logger = LoggerFactory.getLogger(PassService::class.java)

@Service
open class PassService(
    private val contagioProperties: ContagioProperties,
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val authTokenService: AuthTokenService,
    private val deletePassInfoEnvelope: IFindPassInfoEnvelope,
    private val createPass: CreatePass,
    private val updatePass: UpdatePass,
    private val updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope
) {


    /**
     * TODO: queue deletion of image and pass until authToken is known
     */
    open fun delete(serialnumber: String) {
        deletePassInfoEnvelope.execute(serialnumber)
    }

    open fun issue(serialnumber: String) {
        updatePass
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.ISSUED,
                testResult = null,
                validUntil = null
            )?.also {
                notifyAllDevicesWithInstalledSerialNumber.execute(serialnumber)
            }
    }

    open fun expire(serialnumber: String) {
        updatePass
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.EXPIRED,
                testResult = null,
                validUntil = null
            )?.also {
                notifyAllDevicesWithInstalledSerialNumber.execute(serialnumber)
            }
    }

    open fun revoke(serialnumber: String) {
        updatePass
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.REVOKED,
                testResult = null,
                validUntil = null
            )?.also {
                notifyAllDevicesWithInstalledSerialNumber.execute(serialnumber)
            }
    }

    open fun negative(serialnumber: String) {
        updatePass
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.ISSUED,
                testResult = TestResultType.NEGATIVE,
                validUntil = null
            )?.also {
                notifyAllDevicesWithInstalledSerialNumber.execute(serialnumber)
            }
    }

    open fun positive(serialnumber: String) {
        updatePass
            .execute(
                serialNumber = serialnumber,
                issueStatus = IssueStatus.ISSUED,
                testResult = TestResultType.POSITIVE,
                validUntil = null
            )?.also {
                notifyAllDevicesWithInstalledSerialNumber.execute(serialnumber)
            }
    }

    open fun installed(serialnumber: String) {
        updateOnlyPassInfoEnvelope
            .execute(serialnumber) {
                it.copy(
                    passInstallationStatus = PassInstallationStatus.INSTALLED,
                    passInstalled = Instant.now(),
                )
            }
    }

    open fun removed(serialnumber: String) {
        updateOnlyPassInfoEnvelope
            .execute(serialnumber) {
                it.copy(
                    passInstallationStatus = PassInstallationStatus.REMOVED,
                    passInstalled = Instant.now(),
                    passRemoved = Instant.now()
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
            save = save
        )?.let {

            authTokenService.setAuthToken(IdType.SERIALNUMBER, it.passInfoEnvelope.serialNumber, it.authToken)
            authTokenService.setAuthToken(IdType.PASSID, it.passInfo.passId, it.authToken)
            authTokenService.setAuthToken(IdType.IMAGEID, it.passInfo.imageId, it.authToken)

            it
        }
    }

    open fun updatePass(updatePassRequest: UpdatePassRequest) =
        updatePass
            .execute(
                serialNumber = updatePassRequest.serialNumber,
                issueStatus = IssueStatus.ISSUED,
                testResult = updatePassRequest.testResult,
                validUntil = updatePassRequest.validUntil
            )?.let {
                notifyAllDevicesWithInstalledSerialNumber.execute(updatePassRequest.serialNumber)

                it.passInfoEnvelope
            }


    @Value("classpath:certs/contagio-sign.p12")
    private lateinit var contagioSignKeystore: Resource

    open fun sign(data: ByteArray) =
        SignatureBuilder(
            contagioSignKeystore.inputStream,
            contagioProperties.sign.password,
            contagioProperties.sign.keyname
        ).execute(data)

}
