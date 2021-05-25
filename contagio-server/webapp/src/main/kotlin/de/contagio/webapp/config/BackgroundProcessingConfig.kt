package de.contagio.webapp.config

import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.usecase.NotifyAllDevicesWithInstalledSerialNumber
import de.contagio.core.usecase.UpdateOnlyPassInfoEnvelope
import de.contagio.core.usecase.UpdatePass
import de.contagio.webapp.repository.mongodb.PassInfoEnvelopeRepository
import de.contagio.webapp.service.BackgroundProcessingService
import de.contagio.webapp.service.PassCommandProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling


@Configuration
@EnableScheduling
open class BackgroundProcessingConfig {

    @Bean
    @ConditionalOnProperty(value = ["contagio.scheduler.enabled"], matchIfMissing = true, havingValue = "true")
    open fun backgroundProcessingService(
        passInfoEnvelopeRepository: PassInfoEnvelopeRepository,
        passCommandProcessor: PassCommandProcessor,
        getEncryptionKey: IGetEncryptionKey,
        notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
        updatePass: UpdatePass,
        updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope
    ): BackgroundProcessingService {
        return BackgroundProcessingService(
            passInfoEnvelopeRepository,
            passCommandProcessor,
            notifyAllDevicesWithInstalledSerialNumber,
            updatePass,
            updateOnlyPassInfoEnvelope
        )
    }
}
