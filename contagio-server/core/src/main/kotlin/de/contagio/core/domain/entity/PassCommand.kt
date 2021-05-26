package de.contagio.core.domain.entity

import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.ISetEncryptionKey
import de.contagio.core.domain.port.IdType
import de.contagio.core.usecase.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

private var logger = LoggerFactory.getLogger(PassCommand::class.java)

enum class PassCommandExecutionStatus {
    PENDING, FAILED, SUCCESSFUL
}

enum class PassGetKeyStatus {
    NOTIFIED, UNREACHABLE, FOUND
}

data class PassGetKeyResult(val status: PassGetKeyStatus, val key: String? = null)

sealed class PassCommand(
    val serialNumber: String,
    val created: Instant = Instant.now()
) {
    private var notified: Boolean = false

    protected fun getKey(
        getEncryptionKey: IGetEncryptionKey?,
        notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber
    ): PassGetKeyResult {

        if (getEncryptionKey == null)
            return PassGetKeyResult(PassGetKeyStatus.UNREACHABLE)

        val key = getEncryptionKey.execute(IdType.SERIALNUMBER, serialNumber)
        if (key != null)
            return PassGetKeyResult(PassGetKeyStatus.FOUND, key)

        if (!notified) {
            notified = true

            if (!notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber))
                return PassGetKeyResult(PassGetKeyStatus.UNREACHABLE)
        }

        return PassGetKeyResult(PassGetKeyStatus.NOTIFIED)
    }

    abstract fun execute(getEncryptionKey: IGetEncryptionKey? = null): PassCommandExecutionStatus

    fun logMessage(): String = "$this executed"

    fun executionStatus(key: PassGetKeyResult) = when (key.status) {
        PassGetKeyStatus.FOUND -> PassCommandExecutionStatus.SUCCESSFUL
        PassGetKeyStatus.NOTIFIED -> PassCommandExecutionStatus.PENDING
        PassGetKeyStatus.UNREACHABLE -> PassCommandExecutionStatus.FAILED
    }
}

class DeletePassCommand(
    private val deletePass: DeletePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("DeletePassCommand.execute($serialNumber)")

        deletePass.execute(serialNumber)

        return PassCommandExecutionStatus.SUCCESSFUL
    }

    override fun toString() =
        "DeletePassCommand($serialNumber)"
}

class ExpirePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("ExpirePassCommand.execute($serialNumber)")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key.status == PassGetKeyStatus.FOUND)
            updatePass
                .execute(
                    authToken = key.key!!,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.EXPIRED,
                    testResult = null,
                    validUntil = null
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return executionStatus (key)
    }

    override fun toString() =
        "ExpirePassCommand($serialNumber)"
}

class RevokePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("RevokePassCommand.execute()")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key.status == PassGetKeyStatus.FOUND)
            updatePass
                .execute(
                    authToken = key.key!!,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.REVOKED,
                    testResult = null,
                    validUntil = null
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }


        return executionStatus (key)
    }

    override fun toString() =
        "RevokePassCommand($serialNumber)"
}

class IssuePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {
    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("IssuePassCommand.execute()")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key.status == PassGetKeyStatus.FOUND)
            updatePass
                .execute(
                    authToken = key.key!!,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.ISSUED,
                    testResult = null,
                    validUntil = null
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return executionStatus (key)
    }

    override fun toString() =
        "IssuePassCommand($serialNumber)"
}

class NegativePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("NegativePassCommand.execute($serialNumber)")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key.status == PassGetKeyStatus.FOUND)
            updatePass
                .execute(
                    authToken = key.key!!,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.ISSUED,
                    testResult = TestResultType.NEGATIVE,
                    validUntil = Instant.now().plus(3, ChronoUnit.DAYS)
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return executionStatus (key)
    }

    override fun toString() =
        "NegativePassCommand($serialNumber)"
}

class PositivePassCommand(
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("PositivePassCommand.execute()")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key.status == PassGetKeyStatus.FOUND)
            updatePass
                .execute(
                    authToken = key.key!!,
                    serialNumber = serialNumber,
                    issueStatus = IssueStatus.ISSUED,
                    testResult = TestResultType.POSITIVE,
                    validUntil = Instant.now().plus(30, ChronoUnit.DAYS)
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return executionStatus (key)
    }

    override fun toString() =
        "PositivePassCommand($serialNumber)"
}


class InstalledPassCommand(
    private val updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("InstalledPassCommand.execute()")

        updateOnlyPassInfoEnvelope
            .execute(serialNumber) {
                it.copy(
                    deviceInstallationStatus = DeviceInstallationStatus.INSTALLED,
                    deviceUpdated = Instant.now(),
                )
            }

        return PassCommandExecutionStatus.SUCCESSFUL
    }

    override fun toString() =
        "InstalledPassCommand($serialNumber)"
}


class RemovedPassCommand(
    private val updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope,
    serialNumber: String
) : PassCommand(serialNumber) {

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("InstalledPassCommand.execute()")

        updateOnlyPassInfoEnvelope
            .execute(serialNumber) {
                it.copy(
                    deviceInstallationStatus = DeviceInstallationStatus.REMOVED,
                    deviceUpdated = Instant.now()
                )
            }

        return PassCommandExecutionStatus.SUCCESSFUL
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

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
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
            setEncryptionKey.execute(IdType.PASSID, it.passInfoEnvelope.passId, it.authToken)
            setEncryptionKey.execute(IdType.IMAGEID, it.passInfoEnvelope.imageId, it.authToken)
        }

        return PassCommandExecutionStatus.SUCCESSFUL
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

    override fun execute(getEncryptionKey: IGetEncryptionKey?): PassCommandExecutionStatus {
        logger.debug("UpdatePassCommand.execute()")

        val key = getKey(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber)
        if (key.status == PassGetKeyStatus.FOUND)
            updatePass
                .execute(
                    authToken = key.key!!,
                    serialNumber = serialNumber,
                    issueStatus = issueStatus,
                    testResult = testResult,
                    validUntil = validUntil
                )?.also {
                    notifyAllDevicesWithInstalledSerialNumber.execute(serialNumber)
                }

        return executionStatus (key)
    }

    override fun toString() =
        "UpdatePassCommand($serialNumber)"
}
