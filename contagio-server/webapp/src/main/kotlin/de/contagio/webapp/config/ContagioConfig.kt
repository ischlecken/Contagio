package de.contagio.webapp.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan("de.contagio.webapp.model.properties")
open class ContagioConfig
