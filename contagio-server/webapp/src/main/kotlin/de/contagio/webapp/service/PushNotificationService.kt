package de.contagio.webapp.service

import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.ApnsClientBuilder
import com.eatthepath.pushy.apns.PushNotificationResponse
import com.eatthepath.pushy.apns.auth.ApnsSigningKey
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

    private val poolSize = 2

    @Value("classpath:certs/pass.p12")
    private lateinit var passKeystore: Resource

    private var walletClient: ApnsClient? = null
    private var topics: Set<String>? = null

    @Value("\${contagio.apnserver}")
    private lateinit var apnServer: String

    @Value("\${contagio.teststation.bundleid}")
    private lateinit var teststationTopic: String

    @Value("\${contagio.teststation.teamid}")
    private lateinit var teamid: String

    @Value("\${contagio.teststation.keyid}")
    private lateinit var keyid: String

    @Value("classpath:certs/teststation.p8")
    private lateinit var teststationKey: Resource
    private var teststationClient: ApnsClient? = null

    @PostConstruct
    open fun afterInit() {
        try {
            val keystorePassword = contagioProperties.pass.keystorePassword.toCharArray()
            val keyStore = CertUtils.toKeyStore(passKeystore.inputStream, keystorePassword)
            val certificate = CertUtils.extractCertificateWithKey(keyStore, keystorePassword)

            this.walletClient = ApnsClientBuilder()
                .setApnsServer(apnServer, 443)
                .setClientCredentials(
                    certificate.right, certificate.left,
                    contagioProperties.pass.keystorePassword
                )
                .setConcurrentConnections(poolSize)
                .build()
            this.topics = CertUtils.extractApnsTopics(certificate.right)

            this.teststationClient = ApnsClientBuilder()
                .setApnsServer(apnServer, 443)
                .setSigningKey(ApnsSigningKey.loadFromInputStream(teststationKey.inputStream, teamid, keyid))
                .setConcurrentConnections(poolSize)
                .build()

        } catch (ex: CertificateException) {
            logger.error("failed to init PushNotificationService", ex)
        }
    }

    open fun send2WalletAsync(pushtoken: String): PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>? {
        logger.debug("notify wallets using pushtoken $pushtoken ...")

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
        logger.debug("wallets with pushtoken $pushtoken notified.")

        return walletClient?.sendNotification(pushNotification)
    }

    open fun send2TeststationAsync(
        serialNumber: String,
        deviceToken: String
    ): PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>? {
        logger.debug("notify teststation app using deviceToken $deviceToken ...")

        val payloadBuilder = SimpleApnsPayloadBuilder()
        payloadBuilder.setAlertBody("certificate $serialNumber changed.")
        payloadBuilder.addCustomProperty("serialNumber", serialNumber)
        payloadBuilder.addCustomProperty("action", "updated")

        val payload = payloadBuilder.build()
        logger.debug("sending payload $payload to topic $teststationTopic")

        val pushNotification = SimpleApnsPushNotification(deviceToken, teststationTopic, payload)
        logger.debug("teststation app with deviceToken $deviceToken notified.")

        return teststationClient?.sendNotification(pushNotification)
    }
}
