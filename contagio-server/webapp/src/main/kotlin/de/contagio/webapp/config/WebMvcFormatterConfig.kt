package de.contagio.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.datetime.DateFormatter
import org.springframework.format.datetime.standard.InstantFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

const val contagioDisplayDateFormat: String = "dd.MM.yy HH:mm:ss"

@Configuration
open class WebMvcFormatterConfig {

    @Bean
    open fun dateFormatter(): DateFormatter {
        return DateFormatter(contagioDisplayDateFormat)
    }

    @Bean
    open fun instantFormatter(dateFormatter: DateFormatter): InstantFormatter {
        return object : InstantFormatter() {
            override fun print(o: Instant, locale: Locale?): String {
                return o.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(contagioDisplayDateFormat, locale))
            }
        }
    }
}
