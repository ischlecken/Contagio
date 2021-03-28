package de.contagio.webapp.config

import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeAttribute
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ErrorPropertiesConfig {

    @Bean
    open fun myErrorProperties(): ErrorProperties {
        val result = ErrorProperties()

        result.includeMessage = IncludeAttribute.ALWAYS
        result.includeStacktrace = IncludeStacktrace.NEVER
        result.isIncludeException = true

        return result
    }
}
