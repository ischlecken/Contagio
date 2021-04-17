package de.contagio.webapp.config

import de.contagio.webapp.model.SwaggerPageable
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Pageable
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import java.util.*


@Configuration
@ConditionalOnWebApplication
open class SwaggerConfig {

    @Bean
    open fun api(): Docket =
            Docket(DocumentationType.OAS_30)
                    .select()
                    .apis(RequestHandlerSelectors.any())
                    .paths(PathSelectors.regex("/error").negate())
                    .paths(PathSelectors.regex("/actuator.*").negate())
                    .build()
                    .genericModelSubstitutes(Optional::class.java)
                    .directModelSubstitute(Pageable::class.java, SwaggerPageable::class.java)
                    .apiInfo(apiInfo())


    private fun apiInfo() =
            ApiInfo(
                    "Contagio API",
                    "Contagio API :-)",
                    "Version 1",
                    "https://www.contagio.de/agb",
                    Contact("Stefan Thomas", "www.contagio.de", "stefan.t42@gmx.de"),
                    "Special License",
                    "https://www.contagio.de/agb",
                    emptyList()
            )
}
