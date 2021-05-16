package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.IFindPassInfoEnvelope
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.ISetEncryptionKey
import de.contagio.core.usecase.CreatePass
import de.contagio.core.usecase.NotifyAllDevicesWithInstalledSerialNumber
import de.contagio.core.usecase.SignatureBuilder
import de.contagio.core.usecase.UpdatePass
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

private var logger = LoggerFactory.getLogger(PassService::class.java)

@Service
open class PassService(
    private val contagioProperties: ContagioProperties,
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val setEncryptionKey: ISetEncryptionKey,
    private val getEncryptionKey: IGetEncryptionKey,
    private val createPass: CreatePass,
    private val updatePass: UpdatePass,
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val passCommandProcessor: PassCommandProcessor
) {

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

        if (!save)
            return createPass.execute(
                passSigningInfo = passCommandProcessor.passSigningInfo(),
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
                save = false
            )

        passCommandProcessor.addCommand(
            CreatePassCommand(
                setEncryptionKey = setEncryptionKey,
                createPass = createPass,
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
                save = true
            )
        )

        return null
    }

    open fun updatePass(updatePassRequest: UpdatePassRequest): PassInfoEnvelope? {
        val result = findPassInfoEnvelope.execute(updatePassRequest.serialNumber)

        if (result != null)
            passCommandProcessor.addCommand(
                UpdatePassCommand(
                    getEncryptionKey,
                    notifyAllDevicesWithInstalledSerialNumber = notifyAllDevicesWithInstalledSerialNumber,
                    updatePass = updatePass,
                    serialNumber = updatePassRequest.serialNumber,
                    issueStatus = IssueStatus.ISSUED,
                    testResult = updatePassRequest.testResult,
                    validUntil = updatePassRequest.validUntil
                )
            )

        return result
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
