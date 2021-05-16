package de.contagio.webapp

import de.contagio.webapp.config.BuildInfoConfig
import de.contagio.webapp.service.BackgroundProcessingService
import de.contagio.webapp.service.PassCommandProcessor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

private var logger = LoggerFactory.getLogger(ContagioApplication::class.java)

@SpringBootApplication(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class
    ]
)
@ComponentScan(
    basePackages = [
        "de.contagio.webapp.config",
        "de.contagio.webapp.controller",
        "de.contagio.webapp.restcontroller",
        "de.contagio.webapp.service"
    ],
    excludeFilters = [
        Filter(
            type = FilterType.REGEX,
            pattern = ["de.goyellow.common.web.rest.serviceunavailable.mongo.*"]
        )
    ]
)
@EnableMongoRepositories("de.contagio.webapp.repository.mongodb")
open class ContagioApplication(
    @Value("\${spring.application.name}")
    private var appName: String,
    private val buildConfig: BuildInfoConfig,
    private val backgroundProcessingService: BackgroundProcessingService,
    private val passCommandProcessor: PassCommandProcessor
) {

    @PostConstruct
    fun init() {
        val bi = buildConfig.buildInfo

        logger.info("appName:${this.appName}")
        logger.info(bi.scm)
        logger.info("Java Vendor:${System.getProperty("java.vendor")}")
        logger.info("Java Version:${System.getProperty("java.version")}")
        logger.info("Maven Version:${bi.version}")
        logger.info("Git Revision:${bi.revision}")
        logger.info("Git Branch:${bi.scmBranch}")
        logger.info("Build Timestamp:${bi.timestamp}")
        logger.info("Spring Boot:${bi.springboot}")

        passCommandProcessor.start { logger.debug("CommandProcessor stopped.") }
    }

    @PreDestroy
    fun onDestroy() {

        logger.info("time to say goodbye for ${this.appName}...")
        passCommandProcessor.stop()
        backgroundProcessingService.stop()
        logger.info("goodbye ${this.appName}.")
    }
}

fun main(args: Array<String>) {
    runApplication<ContagioApplication>(*args) {
        setAdditionalProfiles("buildinfo")
    }
}
