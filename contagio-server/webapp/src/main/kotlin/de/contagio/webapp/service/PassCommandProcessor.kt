package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.*
import de.contagio.core.usecase.*
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

private var logger = LoggerFactory.getLogger(PassCommandProcessor::class.java)

@Service
open class PassCommandProcessor(
    private val contagioProperties: ContagioProperties,
    private val setEncryptionKey: ISetEncryptionKey,
    private val getEncryptionKey: IGetEncryptionKey,
    private val savePassUpdateLog: ISavePassUpdateLog,
    private val notifyDevices: NotifyAllDevicesWithInstalledSerialNumber,
    private val createPass: CreatePass,
    private val updatePass: UpdatePass,
    private val deletePass: DeletePass,
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val updatePassInfoEnvelope: UpdatePassInfoEnvelope,
    private val findUpdatePassRequest: IFindUpdatePassRequest,
    private val saveUpdatePassRequest: ISaveUpdatePassRequest

) : BackgroundJob() {


    private val uidGeneration = UIDGenerator()
    private val mutex = Mutex()
    private val _commands = mutableListOf<PassCommand>()

    val size: Int get() = _commands.size
    val isProcessing: Boolean get() = _commands.size > 0


    fun revokePass(serialNumber: String) {
        if (!commandExists(serialNumber, RevokePassCommand::class.java))
            addCommand(RevokePassCommand(notifyDevices, saveUpdatePassRequest, serialNumber))
    }

    fun negativeTestresult(serialNumber: String) {
        if (!commandExists(serialNumber, NegativePassCommand::class.java))
            addCommand(NegativePassCommand(notifyDevices, saveUpdatePassRequest, serialNumber))
    }

    fun positiveTestresult(serialNumber: String) {
        if (!commandExists(serialNumber, PositivePassCommand::class.java))
            addCommand(PositivePassCommand(notifyDevices, saveUpdatePassRequest, serialNumber))
    }

    fun expirePass(serialNumber: String) {
        if (!commandExists(serialNumber, ExpirePassCommand::class.java))
            addCommand(
                ExpirePassCommand(
                    notifyDevices,
                    saveUpdatePassRequest,
                    findPassInfoEnvelope,
                    findUpdatePassRequest,
                    serialNumber
                )
            )
    }

    fun deletePass(serialNumber: String) {
        if (!commandExists(serialNumber, DeletePassCommand::class.java))
            addCommand(DeletePassCommand(deletePass, serialNumber))
    }

    fun passInstalled(serialNumber: String) {
        if (!commandExists(serialNumber, InstalledPassCommand::class.java))
            addCommand(InstalledPassCommand(updatePassInfoEnvelope, serialNumber))
    }

    fun passRemoved(serialNumber: String) {
        if (!commandExists(serialNumber, RemovedPassCommand::class.java))
            addCommand(RemovedPassCommand(updatePassInfoEnvelope, serialNumber))
    }

    fun verifyPass(serialNumber: String) {
        if (!commandExists(serialNumber, VerifyPassCommand::class.java))
            addCommand(VerifyPassCommand(notifyDevices, serialNumber))
    }

    fun createPass(
        image: MultipartFile,
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

        logger.debug("createPass(firstName=$firstName, lastName=$lastName, testResult=$testResult)")
        logger.debug("  image.size=${image.size}")

        if (!save)
            return createPass.execute(
                serialNumber = uidGeneration.generate(),
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

        addCommand(
            CreatePassCommand(
                setEncryptionKey = setEncryptionKey,
                createPass = createPass,
                serialNumber = uidGeneration.generate(),
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

    fun updatePass(updatePassRequest: UpdatePassRequest): PassInfoEnvelope? {
        val result = findPassInfoEnvelope.execute(updatePassRequest.serialNumber)

        if (result != null)
            addCommand(
                UpdatePassCommand(
                    notifyAllDevicesWithInstalledSerialNumber = notifyDevices,
                    updatePass = updatePass,
                    serialNumber = updatePassRequest.serialNumber,
                    issueStatus = IssueStatus.ISSUED,
                    testResult = updatePassRequest.testResult,
                    validUntil = updatePassRequest.validUntil
                )
            )

        return result
    }

    fun getCommands(pageable: Pageable): PagedResult<PassCommand> {
        val toIndex = if (size < pageable.pageSize) size else pageable.pageSize
        val content = _commands.subList(0, toIndex)

        return PagedResult(
            content = content,
            isFirst = true,
            isLast = true,
            pageNo = 0,
            pageSize = pageable.pageSize,
            totalPages = 1,
            totalElements = size
        )
    }

    private fun addCommand(cmd: PassCommand) {
        runBlocking {
            mutex.withLock {
                _commands.add(cmd)
            }
        }

        logger.debug("addCommand($cmd)")
    }


    private fun commandExists(serialNumber: String, cmdClass: Class<*>) =
        _commands.firstOrNull { it.serialNumber == serialNumber && it::class.java == cmdClass } != null

    private suspend fun peekCommand(i: Int): PassCommand? {
        var result: PassCommand? = null

        mutex.withLock {
            if (_commands.size > 0 && i < _commands.size)
                result = _commands[i]
        }

        return result
    }

    private suspend fun removeCommand(i: Int) {
        mutex.withLock {
            if (_commands.size > 0 && i < _commands.size)
                _commands.removeAt(i)
        }
    }

    override suspend fun process() {
        logger.debug("CommandProcessor begins... {${Thread.currentThread().name}}")

        while (isRunning()) {
            delay(30000)

            var i = 0
            do {
                val cmd = peekCommand(i)
                if (cmd != null) {
                    val executionStatus = cmd.execute(getEncryptionKey)

                    logger.debug("execute cmd for serialNumber ${cmd.serialNumber}: $executionStatus")

                    when (executionStatus) {
                        PassCommandExecutionStatus.SUCCESSFUL -> {
                            savePassUpdateLog.execute(
                                PassUpdateLog(
                                    serialNumber = cmd.serialNumber,
                                    action = cmd.javaClass.simpleName,
                                    message = cmd.logMessage()
                                )
                            )

                            removeCommand(i)
                        }
                        PassCommandExecutionStatus.FAILED -> {
                            savePassUpdateLog.execute(
                                PassUpdateLog(
                                    serialNumber = cmd.serialNumber,
                                    action = cmd.javaClass.simpleName,
                                    message = "${cmd.javaClass.simpleName} failed."
                                )
                            )

                            removeCommand(i)
                        }
                        else -> i++
                    }
                }
            } while (cmd != null)
        }

        logger.debug("CommandProcessor ends... {${Thread.currentThread().name}}")
    }

}
