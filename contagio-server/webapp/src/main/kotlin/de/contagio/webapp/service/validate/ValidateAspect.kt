package de.contagio.webapp.service.validate

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.entity.Teststation
import de.contagio.core.domain.port.IFindEncryptedPayload
import de.contagio.core.domain.port.IFindPassInfoEnvelope
import de.contagio.core.domain.port.ISetEncryptionKey
import de.contagio.core.domain.port.IdType
import de.contagio.webapp.model.properties.ContagioProperties
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

private var logger = LoggerFactory.getLogger(ValidateAspect::class.java)
private const val APPLE_PASS = "ApplePass "

@Aspect
@Component
open class ValidateAspect(
    private val contagioProperties: ContagioProperties,
    private val findPassInfoEnvelope: IFindPassInfoEnvelope,
    private val findEncryptedPayload: IFindEncryptedPayload,
    private val setEncryptionKey: ISetEncryptionKey
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
                val isDeleted = findPassInfoEnvelope.execute(serialNumber)?.let { pie ->
                    pie.issueStatus == IssueStatus.DELETED
                }

                logger.debug(" serialNumber=$serialNumber isDeleted=$isDeleted")

                if (isDeleted == true)
                    ResponseEntity<Any>(HttpStatus.GONE)
                else if (passTypeIdentifier != null &&
                    serialNumber != null &&
                    authorization != null &&
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


    @Around("@annotation(validate)")
    @Throws(Throwable::class)
    open fun validateTeststation(pjp: ProceedingJoinPoint, validate: ValidateTeststation): Any? {
        return try {
            val cc = pjp.getFirstParameterWithType<Teststation>()

            if (cc != null)
                pjp.proceed()
            else {
                "redirect:/teststation"
            }
        } catch (ex: Exception) {
            logger.error("Exception while validating teststation:{}", ex)

            throw ex
        }
    }

    @Around("@annotation(validate)")
    @Throws(Throwable::class)
    open fun validateTester(pjp: ProceedingJoinPoint, validate: ValidateTester): Any? {
        return try {
            val cc = pjp.getFirstParameterWithType<Tester>()

            if (cc != null)
                pjp.proceed()
            else {
                "redirect:/tester"
            }
        } catch (ex: Exception) {
            logger.error("Exception while validating tester:{}", ex)

            throw ex
        }
    }


    private fun isAuthorized(passTypeIdentifier: String, serialNumber: String, authorization: String): Boolean {
        val result = passTypeIdentifier == contagioProperties.pass.passTypeId &&
                checkauthorization(serialNumber, authorization)

        logger.debug("isAuthorized(serialNumber=$serialNumber, authorization=$authorization): $result")

        return result
    }

    @Suppress("FoldInitializerAndIfToElvis")
    private fun checkauthorization(serialNumber: String, authorization: String): Boolean {
        if (!authorization.startsWith(APPLE_PASS))
            return false

        val authToken = authorization.substring(APPLE_PASS.length)
        if (authToken.length < 10)
            return false

        val passInfoEnvelope = findPassInfoEnvelope.execute(serialNumber) ?: return false
        val encryptedPassInfo = findEncryptedPayload.execute(passInfoEnvelope.passInfoId) ?: return false

        val passInfo = encryptedPassInfo.getObject(authToken, PassInfo::class.java) as? PassInfo
        if (passInfo == null)
            return false

        setEncryptionKey.execute(IdType.SERIALNUMBER, serialNumber, authToken)
        setEncryptionKey.execute(IdType.PASSID, passInfoEnvelope.passId, authToken)
        setEncryptionKey.execute(IdType.IMAGEID, passInfoEnvelope.imageId, authToken)

        return true
    }
}
