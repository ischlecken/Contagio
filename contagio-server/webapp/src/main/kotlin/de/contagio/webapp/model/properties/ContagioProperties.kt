package de.contagio.webapp.model.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

data class SignProperties(
    val keyname: String,
    val password: String
)

data class PassProperties(
    val organisationName: String,
    val description: String,
    val logoText: String,
    val teamIdentifier: String,
    val passTypeId: String,
    val keystorePassword: String,

    val labelColor: String,
    val foregroundColor: String,
    val backgroundColor: String,
)

data class UserProperties(
    val name: String,
    val password: String,
    val roles: Collection<String>
)

@ConstructorBinding
@ConfigurationProperties(prefix = "contagio")
data class ContagioProperties(
    val baseUrl: String,
    val authTokenValidInMinutes: Int,
    val purgeAfterMinutes: Int,
    val pass: PassProperties,
    val sign: SignProperties,
    val users: List<UserProperties>
)
