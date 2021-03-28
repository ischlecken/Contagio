package de.contagio.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.datetime.DateFormatter


@Configuration
open class WebMvcFormatterConfig {

    @Bean
    open fun dateFormatter(): DateFormatter {
        return DateFormatter("dd.MM.yy HH:mm:ss")
    }
}
