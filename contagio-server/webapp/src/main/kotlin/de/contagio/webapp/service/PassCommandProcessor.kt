package de.contagio.webapp.service

import de.contagio.core.domain.entity.PassCommand
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.usecase.PassSerialNumberWithUpdated
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private var logger = LoggerFactory.getLogger(PassCommandProcessor::class.java)

@Service
open class PassCommandProcessor(
    private val getEncryptionKey: IGetEncryptionKey
) : BackgroundJob() {

    private val mutex = Mutex()
    private val commands = mutableListOf<PassCommand>()

    val size: Int get() = commands.size
    val isProcessing: Boolean get() = commands.size > 0

    fun addCommand(cmd: PassCommand) {
        runBlocking {
            mutex.withLock {
                commands.add(cmd)
            }
        }

        logger.debug("addCommand($cmd)")
    }

    fun getPendingSerialNumbers(): Collection<PassSerialNumberWithUpdated> {
        val result = mutableSetOf<PassSerialNumberWithUpdated>()

        runBlocking {
            mutex.withLock {
                commands.forEach {
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
            if (commands.size > 0 && i < commands.size)
                result = commands[i]
        }

        return result
    }

    private suspend fun removeCommand(i: Int) {
        mutex.withLock {
            if (commands.size > 0 && i < commands.size)
                commands.removeAt(i)
        }
    }

    override suspend fun process() {
        logger.debug("CommandProcessor begins... {${Thread.currentThread().name}}")

        while (isRunning()) {
            delay(10000)

            var i = 0
            do {
                val cmd = peekCommand(i)
                if (cmd != null) {
                    val executionSuccessfull = cmd.execute(getEncryptionKey)

                    logger.debug("execute cmd for serialNumber ${cmd.serialNumber}:$executionSuccessfull")

                    if (executionSuccessfull)
                        removeCommand(i)
                    else
                        i++
                }
            } while (cmd != null)
        }

        logger.debug("CommandProcessor ends... {${Thread.currentThread().name}}")
    }

}
