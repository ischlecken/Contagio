package de.contagio.webapp.config

import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.webapp.model.properties.ContagioProperties
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
        contagioProperties: ContagioProperties,
        passInfoEnvelopeRepository: PassInfoEnvelopeRepository,
        passCommandProcessor: PassCommandProcessor,
        getEncryptionKey: IGetEncryptionKey
    ): BackgroundProcessingService {
        return BackgroundProcessingService(
            contagioProperties,
            passInfoEnvelopeRepository,
            passCommandProcessor,
            getEncryptionKey
        )
    }
}
