package de.contagio.webapp.service.authentication

import org.apache.http.HttpStatus
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


private var logger = LoggerFactory.getLogger(AuthorizationAspect::class.java)

@Aspect
@Component
    open class AuthorizationAspect(private val authenticationService: AuthenticationService) {

    @Around("@annotation(validate)")
    @Throws(Throwable::class)
    open fun checkToken(pjp: ProceedingJoinPoint, validate: ValidateAuthorization): Any? {
        var result: Any? = null

        try {
            val requestAttributes = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
            val token = requestAttributes.request.getHeader("Authorization")

            token?.let { t: String ->
                val user = authenticationService.findUserUsingToken(t)
                if (user != null) {
                    logger.debug("checkToken($t) user=$user")

                    result = pjp.proceed()
                } else {
                    requestAttributes.response!!.status = HttpStatus.SC_FORBIDDEN
                }
            }

            if (token == null)
                requestAttributes.response!!.status = HttpStatus.SC_BAD_REQUEST
        } catch (ex: Exception) {
            logger.error("Exception while checking token:{}", ex)

            throw ex
        }

        return result
    }
}
