package de.contagio.webapp.model.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

data class SignProperties(
    val keystore: String,
    val keyname: String,
    val password: String
)

data class PassProperties(
    val organisationName: String,
    val description: String,
    val logoText: String,
    val teamIdentifier: String,
    val passTypeId: String,
    val keyName: String,
    val privateKeyPassword: String,

    val labelColor: String,
    val foregroundColor: String,
    val backgroundColor: String,
)

@ConstructorBinding
@ConfigurationProperties(prefix = "contagio")
data class ContagioProperties(
    val baseUrl: String,
    val pass: PassProperties,
    val sign: SignProperties
)
