@file:Suppress("unused")

package de.contagio.webapp.service

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

private var logger = LoggerFactory.getLogger(BackgroundJob::class.java)

abstract class BackgroundJob {
    private var job: Job? = null
    private val mutex = Mutex()

    abstract suspend fun process()

    fun start(backgroundJobStopped: () -> Unit) {
        if (job == null) {
            job = GlobalScope.launch {
                try {
                    process()
                } catch (ex: CancellationException) {
                    logger.debug(ex.message)
                } finally {
                    mutex.withLock {
                        job = null
                    }
                    backgroundJobStopped()
                }
            }
        }
    }

    fun stop() {
        if (job != null)
            runBlocking {
                mutex.withLock {
                    if (job != null)
                        job!!.cancel("stop BackgroundJob...")
                }
            }
    }

    fun isRunning() =
        job != null
}
