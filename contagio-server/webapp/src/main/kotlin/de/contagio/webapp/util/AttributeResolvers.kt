package de.contagio.webapp.util

import de.contagio.core.domain.entity.User
import de.contagio.webapp.service.authentication.AuthenticationService
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class UserAttributeResolver(
    private val authenticationService: AuthenticationService
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == User::class.java
    }

    @Throws(Exception::class)
    override fun resolveArgument(
        parameter: MethodParameter?,
        mvContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return webRequest.getHeader("Authorization")?.let { t: String ->
            authenticationService.findUserUsingToken(t)
        }
    }
}
