package de.contagio.webapp.service

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.webapp.repository.mongodb.PassInfoEnvelopeRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.time.temporal.ChronoUnit

private var logger = LoggerFactory.getLogger(BackgroundProcessingService::class.java)

private const val CHECKINTERVAL_IN_HOURS = 20


open class BackgroundProcessingService(
    private val passInfoEnvelopeRepository: PassInfoEnvelopeRepository,
    private val passCommandProcessor: PassCommandProcessor
) : BackgroundJob() {

    private var lastSuccessfullRun: Instant =
        Instant.now().minus((CHECKINTERVAL_IN_HOURS - 1).toLong(), ChronoUnit.HOURS)

    @Scheduled(
        initialDelayString = "\${contagio.scheduler.initialDelayInMilliseconds}",
        fixedDelayString = "\${contagio.scheduler.fixedDelayInMilliseconds}"
    )
    open fun scheduledStart() {
        logger.debug("scheduledStart(): {${Thread.currentThread().name}}")
        start {
            logger.debug("BackgroundProcessingService stopped. {${Thread.currentThread().name}}")
        }
    }


    override suspend fun process() {
        logger.debug("BackgroundProcessingService begins... {${Thread.currentThread().name}}")

        val now = Instant.now()
        val passInfos = passInfoEnvelopeRepository.findByIssueStatus(IssueStatus.ISSUED)

        passInfos.forEach { passInfoEnvelope ->
            if (passInfoEnvelope.validUntil?.isBefore(now) == true) {

                logger.debug("${passInfoEnvelope.serialNumber} is expired...")

                passCommandProcessor.expirePass(passInfoEnvelope.serialNumber)
            }
        }

        lastSuccessfullRun = Instant.now()

        logger.debug("BackgroundProcessingService ends... {${Thread.currentThread().name}}")
    }

}
