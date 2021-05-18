package de.contagio.webapp.service

import de.contagio.core.domain.entity.PassCommand
import de.contagio.core.domain.port.IGetEncryptionKey
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

    private suspend fun peekCommand(i: Int): PassCommand? {
        var result: PassCommand? = null

        mutex.withLock {
            if (commands.size > 0 && i < commands.size)
                result = commands[i]
        }

        logger.debug("peekCommand($i): $result")

        return result
    }

    private suspend fun removeCommand(i: Int) {
        mutex.withLock {
            if (commands.size > 0 && i < commands.size)
                commands.removeAt(i)
        }

        logger.debug("removeCommand($i)")
    }

    override suspend fun process() {
        logger.debug("CommandProcessor begins... {${Thread.currentThread().name}}")

        while (isRunning()) {
            delay(10000)

            var i = 0
            do {
                val cmd = peekCommand(i)

                if (cmd?.execute(getEncryptionKey) == true)
                    removeCommand(i)
                else
                    i++

            } while (cmd != null)

            logger.debug("CommandProcessor ping {${Thread.currentThread().name}}")
        }

        logger.debug("CommandProcessor ends... {${Thread.currentThread().name}}")
    }

}
