package de.contagio.webapp.service

import de.contagio.core.domain.entity.DeviceInstallationStatus
import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.IdType
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassInfoEnvelopeRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.time.temporal.ChronoUnit

private var logger = LoggerFactory.getLogger(BackgroundProcessingService::class.java)

private const val CHECKINTERVAL_IN_HOURS = 20


open class BackgroundProcessingService(
    private val contagioProperties: ContagioProperties,
    private val passInfoEnvelopeRepository: PassInfoEnvelopeRepository,
    private val passCommandProcessor: PassCommandProcessor,
    private val getEncryptionKey: IGetEncryptionKey
) : BackgroundJob() {

    private var lastSuccessfullRun: Instant =
        Instant.now().minus((CHECKINTERVAL_IN_HOURS - 1).toLong(), ChronoUnit.HOURS)

    @Scheduled(
        initialDelayString = "\${contagio.scheduler.initialDelayInMilliseconds}",
        fixedDelayString = "\${contagio.scheduler.fixedDelayInMilliseconds}"
    )
    open fun scheduledStart() {
        start()
    }


    override suspend fun process() {
        logger.debug("BackgroundProcessingService begins... {${Thread.currentThread().name}}")
        val now = Instant.now()

        passInfoEnvelopeRepository
            .findByIssueStatus(IssueStatus.ISSUED)
            .forEach { pie ->
                if (pie.validUntil?.isBefore(now) == true) {
                    passCommandProcessor.expirePass(pie.serialNumber)
                }
            }

        passInfoEnvelopeRepository
            .findByDeviceInstallationStatus(DeviceInstallationStatus.PENDING)
            .forEach { pie ->
                if (pie.issueStatus != IssueStatus.DELETED &&
                    getEncryptionKey.execute(IdType.SERIALNUMBER, pie.serialNumber) == null
                ) {
                    passCommandProcessor.deletePass(pie.serialNumber)
                }
            }

        passInfoEnvelopeRepository
            .findByDeviceInstallationStatus(DeviceInstallationStatus.REMOVED)
            .forEach { pie ->
                if (pie.issueStatus != IssueStatus.DELETED ) {
                    passCommandProcessor.deletePass(pie.serialNumber)
                }
            }

        passInfoEnvelopeRepository
            .findByIssueStatus(IssueStatus.DELETED)
            .forEach { pie ->
                if (pie.updated
                        .plusMillis(contagioProperties.purgeAfterMinutes.toLong() * 60 * 1000)
                        .isBefore(now)
                ) {
                    logger.debug("purge ${pie.serialNumber}...")
                    passInfoEnvelopeRepository.deleteById(pie.serialNumber)
                }
            }

        lastSuccessfullRun = Instant.now()
        logger.debug("BackgroundProcessingService ends... {${Thread.currentThread().name}}")
    }

}
