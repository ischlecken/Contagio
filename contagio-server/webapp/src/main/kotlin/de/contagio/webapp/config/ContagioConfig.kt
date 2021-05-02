package de.contagio.webapp.config

import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.format.DateTimeFormatterBuilder


private var logger = LoggerFactory.getLogger(ContagioConfig::class.java)

private const val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss"

@Configuration
@ConfigurationPropertiesScan("de.contagio.webapp.model.properties")
open class ContagioConfig {

    @Bean
    open fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        logger.debug("jsonCustomizer()")

        val df = DateTimeFormatterBuilder().appendInstant(0).toFormatter()

        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder.simpleDateFormat(dateTimeFormat)

            builder.serializers(object : InstantSerializer(InstantSerializer.INSTANCE, false, df) {})
        }
    }
}
