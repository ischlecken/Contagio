package de.contagio.webapp.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.*

data class BuildInfo(
        var springboot: String,
        var scm: String,
        var version: String,
        var revision: String,
        var scmBranch: String,
        var timestamp: Date? = null
)


@Configuration
@ConfigurationProperties(prefix = "build")
open class BuildInfoConfig(
        var springboot: String = "",
        var scm: String = "",
        var version: String = "",
        var revision: String = "",
        var branch: String = "",
        var timestamp: String = ""
) {

    val buildInfo: BuildInfo
        get() {
            val result = BuildInfo(springboot, scm, version, revision, scmBranch = branch)

            if (timestamp.isNotEmpty()) {
                try {
                    result.timestamp = Date(timestamp.toLong())
                } catch (ex: NumberFormatException) {
                }
            }
            return result
        }
}
