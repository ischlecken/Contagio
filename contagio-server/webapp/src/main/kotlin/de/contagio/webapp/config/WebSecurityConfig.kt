package de.contagio.webapp.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.factory.PasswordEncoderFactories


@Configuration
@EnableWebSecurity
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

        auth
            .inMemoryAuthentication()
            .withUser("contagiouser")
            .password(encoder.encode("contagiouser123"))
            .roles("USER")
            .and()
            .withUser("contagioadmin")
            .password(encoder.encode("contagioadmin123"))
            .roles("USER", "ADMIN")
            .and()
            .withUser("contagioapi")
            .password(encoder.encode("contagioapi123"))
            .roles("API")
    }

    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers(
                "/",
                "/mcwj/**",
                "/mcstatic/**",
                "/showpass",
                "/co_v1/pass/image/*",
                "/co_v1/wallet/**"
            ).permitAll()
            .antMatchers(
                "/swagger-ui/**",
                "/createpass",
                "/teststation",
                "/tester"
            ).hasRole("ADMIN")
            .antMatchers("/co_v1/**").hasRole("API")
            .anyRequest().authenticated()
            .and().httpBasic().realmName("contagio")
            .and().cors()
            .and().csrf().disable()

        /*
        http
                .authorizeRequests().antMatchers("**").permitAll()
                .and().cors()
                .and().csrf().disable()

         */

    }

    override fun configure(web: WebSecurity) {
        web.debug(true)
    }
}
