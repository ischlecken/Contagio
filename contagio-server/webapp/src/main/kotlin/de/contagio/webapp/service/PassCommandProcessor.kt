package de.contagio.webapp.service

import de.contagio.core.domain.entity.PassCommand
import de.contagio.core.domain.entity.PassCommandExecutionStatus
import de.contagio.core.domain.entity.PassUpdateLog
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.ISavePassUpdateLog
import de.contagio.core.domain.port.PagedResult
import de.contagio.core.usecase.PassSerialNumberWithUpdated
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private var logger = LoggerFactory.getLogger(PassCommandProcessor::class.java)

@Service
open class PassCommandProcessor(
    private val getEncryptionKey: IGetEncryptionKey,
    private val savePassUpdateLog: ISavePassUpdateLog
) : BackgroundJob() {

    private val mutex = Mutex()
    private val _commands = mutableListOf<PassCommand>()

    val size: Int get() = _commands.size
    val isProcessing: Boolean get() = _commands.size > 0

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

    fun addCommand(cmd: PassCommand) {
        runBlocking {
            mutex.withLock {
                _commands.add(cmd)
            }
        }

        logger.debug("addCommand($cmd)")
    }

    fun getPendingSerialNumbers(): Collection<PassSerialNumberWithUpdated> {
        val result = mutableSetOf<PassSerialNumberWithUpdated>()

        runBlocking {
            mutex.withLock {
                _commands.forEach {
                    result.add(
                        PassSerialNumberWithUpdated(
                            it.serialNumber,
                            it.created
                        )
                    )
                }
            }
        }

        logger.debug("getPendingSerialNumbers(): $result")

        return result
    }

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
