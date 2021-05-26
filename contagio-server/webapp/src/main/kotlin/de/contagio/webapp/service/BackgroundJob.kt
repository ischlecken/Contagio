@file:Suppress("unused")

package de.contagio.webapp.service

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

private var logger = LoggerFactory.getLogger(BackgroundJob::class.java)

abstract class BackgroundJob {
    private var job: Job? = null

    abstract suspend fun process()

    fun start(backgroundJobStopped: (() -> Unit)? = null) {
        if (job == null) {
            job = GlobalScope.launch {
                try {
                    process()
                } catch (ex: CancellationException) {
                    logger.debug(ex.message)
                } finally {
                    if (backgroundJobStopped != null)
                        backgroundJobStopped()

                    job = null
                }
            }
        }
    }

    fun stop() {
        if (isRunning())
            runBlocking {
                job!!.cancel("stop BackgroundJob...")
                job!!.join()

                job = null
            }
    }

    fun isRunning() =
        job != null && job!!.isActive
}
