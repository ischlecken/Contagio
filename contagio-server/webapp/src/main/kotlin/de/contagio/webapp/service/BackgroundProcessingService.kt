package de.contagio.webapp.service

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.time.temporal.ChronoUnit


private var logger = LoggerFactory.getLogger(BackgroundProcessingService::class.java)

private const val CHECKINTERVAL_IN_HOURS = 20


open class BackgroundProcessingService(
    private val passInfoRepository: PassInfoRepository,
    private val passService: PassService
) : BackgroundJob() {

    private var lastSuccessfullRun: Instant =
        Instant.now().minus((CHECKINTERVAL_IN_HOURS - 1).toLong(), ChronoUnit.HOURS)

    @Scheduled(
        initialDelayString = "\${contagio.scheduler.initialDelayInMilliseconds}",
        fixedDelayString = "\${contagio.scheduler.fixedDelayInMilliseconds}"
    )
    open fun scheduledStart() {
        start {
            logger.debug("BackgroundProcessingService stopped.")
        }
    }


    override suspend fun process() {
        logger.debug("BackgroundProcessingService begins...")

        val now = Instant.now()
        val passInfos = passInfoRepository.findByIssueStatusNotEqual(IssueStatus.EXPIRED)

        passInfos.forEach { passInfo ->
            if (passInfo.validUntil?.isBefore(now) == true) {

                logger.debug("${passInfo.serialNumber} is expired...")

                passService.expire(passInfo.serialNumber)
            }
        }

        lastSuccessfullRun = Instant.now()

        logger.debug("BackgroundProcessingService ends...")
    }

}
