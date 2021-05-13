package de.contagio.webapp.config

import de.contagio.webapp.repository.mongodb.PassInfoEnvelopeRepository
import de.contagio.webapp.service.BackgroundProcessingService
import de.contagio.webapp.service.PassService
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
        passService: PassService
    ): BackgroundProcessingService {
        return BackgroundProcessingService(passInfoEnvelopeRepository, passService)
    }
}
