package de.contagio.webapp.config

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.format.DateTimeFormatter


@Configuration
@ConfigurationPropertiesScan("de.contagio.webapp.model.properties")
open class ContagioConfig {

    private val dateFormat = "yyyy-MM-dd"
    private val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS"

    @Bean
    open fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer? {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder.simpleDateFormat(dateTimeFormat)
            builder.serializers(LocalDateSerializer(DateTimeFormatter.ofPattern(dateFormat)))
            builder.serializers(LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat)))
        }
    }
}
