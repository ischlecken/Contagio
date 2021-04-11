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
    val teamIdentifier: String,
    val passTypeId: String,
    val keyName: String,
    val privateKeyPassword: String,
    val templateName: String,
    val passResourcesDir: String,
    val baseUrl: String,

    val passOrganisationName: String,
    val passDescription: String,
    val passLogoText: String,

    val bpRestapi: MmcontrolBenutzerportalRestapiProperties
)
