package de.contagio.webapp.config
import de.contagio.webapp.model.properties.ContagioProperties
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

private var logger = LoggerFactory.getLogger(RestTemplateConfig::class.java)

@Configuration
open class RestTemplateConfig(
    private val contagioProperties: ContagioProperties
) {


    @Bean
    open fun bpRestapiRestTemplate(): RestTemplate {
        val httpClientConnectionManager = PoolingHttpClientConnectionManager()
        httpClientConnectionManager.maxTotal = 10
        httpClientConnectionManager.defaultMaxPerRoute = 3

        val result = RestTemplate(
            HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create()
                    .setConnectionManager(httpClientConnectionManager)
                    .setDefaultRequestConfig(
                        RequestConfig
                            .custom()
                            .setSocketTimeout(1000)
                            .setConnectTimeout(1000)
                            .build()
                    )
                    .build()
            )
        )
        result.interceptors = listOf(
            BasicAuthenticationInterceptor(
                contagioProperties.bpRestapi.user,
                contagioProperties.bpRestapi.password
            )
        )

        result.errorHandler = object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse) =
                when (response.statusCode) {
                    HttpStatus.OK,
                    HttpStatus.NOT_FOUND -> false
                    else -> true
                }

            override fun handleError(response: ClientHttpResponse) {
                logger.debug("handleError(response=${response.statusCode},${response.rawStatusCode})")
            }

        }

        return result
    }
}
