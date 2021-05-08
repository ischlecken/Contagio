package de.contagio.webapp.config

import de.contagio.webapp.model.properties.ContagioProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.factory.PasswordEncoderFactories

private val logger = LoggerFactory.getLogger(WebSecurityConfig::class.java)

@Configuration
@EnableWebSecurity
open class WebSecurityConfig(private val contagioProperties: ContagioProperties) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

        var authManagerBuilder = auth.inMemoryAuthentication()

        for (i in contagioProperties.users.indices) {
            val it = contagioProperties.users[i]
            val roles = it.roles.toTypedArray()

            logger.debug("known user $it.name")

            val userBuilder = authManagerBuilder
                .withUser(it.name)
                .password(encoder.encode(it.password))
                .roles(*roles)

            if (i < contagioProperties.users.size - 1)
                authManagerBuilder = userBuilder.and()
        }


    }

    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers(
                "/",
                "/mcwj/**",
                "/mcstatic/**",
                "/verify",
                "/co_v1/pass/*",
                "/co_v1/pass/*/qrcode",
                "/co_v1/pass/image/*",
                "/co_v1/wallet/**"
            ).permitAll()
            .antMatchers(
                "/swagger-ui/**",
                "/createpass",
                "/teststation",
                "/tester",
                "/registrationinfo",
                "/deviceinfo"
                ).hasRole("ADMIN")
            .antMatchers("/co_v1/**").hasRole("API")
            .anyRequest().authenticated()
            .and().httpBasic().realmName("contagio")
            .and().cors()
            .and().csrf().disable()
    }

    override fun configure(web: WebSecurity) {
        web.debug(false)
    }
}
