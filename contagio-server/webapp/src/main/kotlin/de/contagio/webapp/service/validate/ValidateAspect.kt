package de.contagio.webapp.service.validate

import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

private var logger = LoggerFactory.getLogger(ValidateAspect::class.java)

@Aspect
@Component
open class ValidateAspect(
    private val contagioProperties: ContagioProperties,
    private val passInfoRepository: PassInfoRepository
) {

    @Around("@annotation(validate)")
    @Throws(Throwable::class)
    open fun validateApplePass(pjp: ProceedingJoinPoint, validate: ValidateApplePass): Any? {
        logger.debug("ValidateAspect.validateApplePass()")

        return try {
            pjp.getFirstParameterWithType<HttpServletRequest>()?.let {
                val authorization = it.getHeader("Authorization")
                val passTypeIdentifier = pjp.getParameterWithName<String>("passTypeIdentifier")
                val serialNumber = pjp.getParameterWithName<String>("serialNumber")

                if (passTypeIdentifier != null &&
                    serialNumber != null &&
                    isAuthorized(
                        passTypeIdentifier,
                        serialNumber,
                        authorization
                    )
                )
                    pjp.proceed() as ResponseEntity<*>
                else
                    ResponseEntity<Any>(HttpStatus.UNAUTHORIZED)
            } ?: ResponseEntity<Any>(HttpStatus.BAD_REQUEST)
        } catch (ex: Exception) {
            logger.error("Exception while validating:{}", ex)

            throw ex
        }
    }

    @Around("@annotation(validatePassTypeIdentifier)")
    @Throws(Throwable::class)
    open fun validatePassTypeIdentifier(
        pjp: ProceedingJoinPoint,
        validatePassTypeIdentifier: ValidatePassTypeIdentifier
    ): Any? {
        return pjp.getParameterWithName<String>("passTypeIdentifier")?.let {
            logger.debug("ValidateAspect.validatePassTypeIdentifier($it)")

            if (it == contagioProperties.pass.passTypeId)
                pjp.proceed() as ResponseEntity<*>
            else
                ResponseEntity<Any>(HttpStatus.BAD_REQUEST)
        } ?: ResponseEntity<Any>(HttpStatus.BAD_REQUEST)
    }

    private fun isAuthorized(passTypeIdentifier: String, serialNumber: String, authorization: String): Boolean {
        var result = true

        if (passTypeIdentifier != contagioProperties.pass.passTypeId)
            result = false
        else {
            val passInfo = passInfoRepository.findById(serialNumber)

            if (passInfo.isPresent && "ApplePass ${passInfo.get().authToken}" != authorization) {
                logger.debug("authorization $authorization differs from expected value 'ApplePass ${passInfo.get().authToken}'")

                result = false
            }
        }

        logger.debug("isAuthorized(serialNumber=$serialNumber,authorization=$authorization):$result")

        return result
    }
}
