package de.contagio.core.domain.entity

import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.ISetEncryptionKey
import de.contagio.core.domain.port.IdType
import de.contagio.core.usecase.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

private var logger = LoggerFactory.getLogger(PassCommand::class.java)

sealed class PassCommand(
    val serialNumber: String,
    val created: Instant = Instant.now()
) {
    private var notified: Boolean = false

    protected fun getKey(
        getEncryptionKey: IGetEncryptionKey?,
        notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber
    ): String? {

        if (getEncryptionKey == null)
            return null

        val key = getEncryptionKey.execute(IdType.SERIALNUMBER, serialNumber)
        if (key == null && !notified) {
            notified = true

            notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
        }

        return key
    }

    abstract fun execute(getEncryptionKey: IGetEncryptionKey? = null): Boolean
}

class DeletePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val deletePass: DeletePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("DeletePassCommand.execute($serialNumber)")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key != null)
            deletePass.execute(key, serialNumber)

        return key != null
    }

    override fun toString() =
        "DeletePassCommand($serialNumber)"
}

class ExpirePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("ExpirePassCommand.execute($serialNumber)")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key != null)
            updatePass
                .execute(
                    authToken = key,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.EXPIRED,
                    testResult = null,
                    validUntil = null
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return key != null
    }

    override fun toString() =
        "ExpirePassCommand($serialNumber)"
}

class RevokePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("RevokePassCommand.execute()")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key != null)
            updatePass
                .execute(
                    authToken = key,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.REVOKED,
                    testResult = null,
                    validUntil = null
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return key != null
    }

    override fun toString() =
        "RevokePassCommand($serialNumber)"
}

class IssuePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {
    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("IssuePassCommand.execute()")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key != null)
            updatePass
                .execute(
                    authToken = key,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.ISSUED,
                    testResult = null,
                    validUntil = null
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return key != null
    }

    override fun toString() =
        "IssuePassCommand($serialNumber)"
}

class NegativePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("NegativePassCommand.execute($serialNumber)")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key != null)
            updatePass
                .execute(
                    authToken = key,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.ISSUED,
                    testResult = TestResultType.NEGATIVE,
                    validUntil = Instant.now().plus(3, ChronoUnit.DAYS)
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return key != null
    }

    override fun toString() =
        "NegativePassCommand($serialNumber)"
}

class PositivePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("PositivePassCommand.execute()")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key != null)
            updatePass
                .execute(
                    authToken = key,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.ISSUED,
                    testResult = TestResultType.POSITIVE,
                    validUntil = Instant.now().plus(30, ChronoUnit.DAYS)
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return key != null
    }

    override fun toString() =
        "PositivePassCommand($serialNumber)"
}


class InstalledPassCommand(
    private val updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("InstalledPassCommand.execute()")

        updateOnlyPassInfoEnvelope
            .execute(serialNumber) {
                it.copy(
                    passInstallationStatus = PassInstallationStatus.INSTALLED,
                    passInstalled = Instant.now(),
                )
            }

        return true
    }

    override fun toString() =
        "InstalledPassCommand($serialNumber)"
}


class RemovedPassCommand(
    private val updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("InstalledPassCommand.execute()")

        updateOnlyPassInfoEnvelope
            .execute(serialNumber) {
                it.copy(
                    passInstallationStatus = PassInstallationStatus.REMOVED,
                    passInstalled = Instant.now(),
                    passRemoved = Instant.now()
                )
            }

        return true
    }

    override fun toString() =
        "RemovedPassCommand($serialNumber)"
}

class CreatePassCommand(
    private val setEncryptionKey: ISetEncryptionKey,
    private val createPass: CreatePass,
    serialNumber: String,
    private val teamIdentifier: String,
    private val passTypeIdentifier: String,
    private val organisationName: String,
    private val description: String,
    private val logoText: String,
    private val image: ByteArray,
    private val firstName: String,
    private val lastName: String,
    private val phoneNo: String,
    private val email: String?,
    private val testerId: String,
    private val testResult: TestResultType,
    private val testType: TestType,
    private val passType: PassType,
    private val labelColor: String,
    private val foregroundColor: String,
    private val backgroundColor: String,
    private val save: Boolean
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("CreatePassCommand.execute()")

        createPass.execute(
            serialNumber = serialNumber,
            teamIdentifier = teamIdentifier,
            passTypeIdentifier = passTypeIdentifier,
            organisationName = organisationName,
            description = description,
            logoText = logoText,
            image = image,
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
        )?.also {

            setEncryptionKey.execute(IdType.SERIALNUMBER, it.passInfoEnvelope.serialNumber, it.authToken)
            setEncryptionKey.execute(IdType.PASSID, it.passInfo.passId, it.authToken)
            setEncryptionKey.execute(IdType.IMAGEID, it.passInfo.imageId, it.authToken)
        }

        return true
    }

    override fun toString() =
        "CreatePassCommand()"
}


class UpdatePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String,
    private val issueStatus: IssueStatus,
    private val testResult: TestResultType?,
    private val validUntil: Instant?
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): Boolean {
        logger.debug("UpdatePassCommand.execute()")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key != null)
            updatePass
                .execute(
                    authToken = key,
                    serialNumber = serialNumber,
                    issueStatus = issueStatus,
                    testResult = testResult,
                    validUntil = validUntil
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return key != null
    }

    override fun toString() =
        "UpdatePassCommand($serialNumber)"
}
