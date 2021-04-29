package de.contagio.webapp.config

import de.contagio.webapp.repository.mongodb.PassInfoRepository
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
        passInfoRepository: PassInfoRepository,
        passService: PassService
    ): BackgroundProcessingService {
        return BackgroundProcessingService(passInfoRepository, passService)
    }
}
