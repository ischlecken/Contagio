package de.contagio.webapp.config

import de.contagio.core.usecase.SearchTesterWithTeststation
import de.contagio.core.usecase.SearchTeststation
import de.contagio.webapp.util.TesterAttributeResolver
import de.contagio.webapp.util.TeststationAttributeResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.format.datetime.DateFormatter
import org.springframework.format.datetime.standard.InstantFormatter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver

@Configuration
open class WebMvcConfig(
    private val dateFormatter: DateFormatter,
    private val instantFormatter: InstantFormatter,
    private val searchTesterWithTeststation: SearchTesterWithTeststation,
    private val searchTeststation: SearchTeststation
) : WebMvcConfigurer {

    @Bean
    open fun localeResolver(): LocaleResolver {
        val slr = SessionLocaleResolver()
        //slr.setDefaultLocale(Locale.GERMANY)

        return slr
    }

    @Bean
    open fun localeChangeInterceptor(): LocaleChangeInterceptor {
        val lci = LocaleChangeInterceptor()
        lci.paramName = "lang"

        return lci
    }


    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeChangeInterceptor())
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addFormatter(dateFormatter)
        registry.addFormatter(instantFormatter)
    }


    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(TesterAttributeResolver(searchTesterWithTeststation))
        argumentResolvers.add(TeststationAttributeResolver(searchTeststation))
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registerResourceHandlerWithCachePeriod(
            registry
                .addResourceHandler("/mcwj/**")
                .addResourceLocations("/webjars/")
        )

        registerResourceHandlerWithCachePeriod(
            registry
                .addResourceHandler("/mcstatic/**")
                .addResourceLocations(
                    "classpath:/META-INF/resources/",
                    "classpath:/resources/",
                    "classpath:/static/"
                )
        )

        registerResourceHandlerWithCachePeriod(
            registry
                .addResourceHandler("/apps/**")
                .addResourceLocations("classpath:/apps/")
        )
    }

    private fun registerResourceHandlerWithCachePeriod(
        registry: ResourceHandlerRegistration,
        cacheperiodInMinutes: Int = 0
    ) {
        if (cacheperiodInMinutes > 0)
            registry
                .setCachePeriod(60 * cacheperiodInMinutes)
                .resourceChain(true)
        else
            registry
                .resourceChain(true)
    }

}
