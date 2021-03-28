package de.contagio.webapp.model.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

data class MmcontrolBenutzerportalRestapiProperties(
    val baseurl: String,
    val user: String,
    val password: String
)

@ConstructorBinding
@ConfigurationProperties(prefix = "contagio")
data class ContagioProperties(
    val useMinifiedResources: Boolean,

    val bpRestapi: MmcontrolBenutzerportalRestapiProperties
)
