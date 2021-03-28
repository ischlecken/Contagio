package de.contagio.webapp.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter


@Configuration
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
                .authorizeRequests().antMatchers("/**").permitAll()
                .and().cors()
                .and().csrf().disable()

    }

    override fun configure(web: WebSecurity) {
        web.debug(false)
    }
}
