package de.contagio.webapp.service

import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.ApnsClientBuilder
import com.eatthepath.pushy.apns.PushNotificationResponse
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification
import com.eatthepath.pushy.apns.util.TokenUtil
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture
import de.brendamour.jpasskit.util.Assert
import de.brendamour.jpasskit.util.CertUtils
import de.contagio.webapp.model.properties.ContagioProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.security.cert.CertificateException
import javax.annotation.PostConstruct

private var logger = LoggerFactory.getLogger(PushNotificationService::class.java)

@Service
open class PushNotificationService(
    private val contagioProperties: ContagioProperties
) {

    @Value("classpath:certs/pass.p12")
    private lateinit var passKeystore: Resource

    private var client: ApnsClient? = null
    private var topics: Set<String>? = null
    private val poolSize = 10

    @PostConstruct
    open fun afterInit() {
        try {
            val keystorePassword = contagioProperties.pass.keystorePassword.toCharArray()
            val keyStore = CertUtils.toKeyStore(passKeystore.inputStream, keystorePassword)
            val certificate = CertUtils.extractCertificateWithKey(keyStore, keystorePassword)

            this.client = ApnsClientBuilder()
                .setApnsServer("api.push.apple.com", 443)
                .setClientCredentials(
                    certificate.right,
                    certificate.left,
                    contagioProperties.pass.keystorePassword
                )
                .setConcurrentConnections(poolSize)
                .build()
            this.topics = CertUtils.extractApnsTopics(certificate.right)
        } catch (ex: CertificateException) {
            logger.error("failed to init PushNotificationService", ex)
        }
    }

    open fun sendPushNotificationAsync(pushtoken: String): PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>? {
        logger.debug("sending push notification for pushtoken $pushtoken ...")

        val payloadBuilder = SimpleApnsPayloadBuilder()
        payloadBuilder.setAlertBody("{}")
        val payload: String = payloadBuilder.build()
        val token: String = TokenUtil.sanitizeTokenString(pushtoken)
        Assert.state(topics!!.isNotEmpty(), "APNS topic is required for sending a push notification", *arrayOfNulls(0))
        var topic: String? = null
        if (topics!!.isNotEmpty()) {
            topic = topics!!.iterator().next()
            if (topics!!.size > 1) {
                logger.warn("Multiple APNS topics detected, using $topic (first value out of ${topics!!.size} available) for sending a push notification")
            }
        }
        val pushNotification = SimpleApnsPushNotification(token, topic, payload)
        logger.debug("push notification for pushtoken $pushtoken send.")

        return client?.sendNotification(pushNotification)
    }
}
