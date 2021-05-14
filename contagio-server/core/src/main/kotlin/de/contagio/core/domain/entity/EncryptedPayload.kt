package de.contagio.core.domain.entity

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.contagio.core.usecase.Decryptor
import de.contagio.core.usecase.Encryptor
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import java.time.Instant
import java.time.format.DateTimeFormatterBuilder

private var logger = LoggerFactory.getLogger(EncryptedPayload::class.java)

private val om = jacksonObjectMapper().apply {
    val javaTimeModule = JavaTimeModule()

    javaTimeModule.addSerializer(
        Instant::class.java,
        object : InstantSerializer(
            INSTANCE,
            false,
            DateTimeFormatterBuilder().appendInstant(0).toFormatter()
        ) {
        }
    )
    registerModule(javaTimeModule)
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}

interface IEncryptedPayload {
    fun getObject(key: String?, cls: Class<*>): Any?
    fun get(key: String?): ByteArray?
}

class EncryptedPayload(
    @Id val id: String,
    val iv: String,
    val encryptedPayload: ByteArray,
    val updated: Instant = Instant.now()
) : IEncryptedPayload {

    override fun getObject(key: String?, cls: Class<*>): Any? {
        var result: Any? = null

        get(key)?.let {
            try {
                result = om.readValue(it, cls)
            } catch (ex: Exception) {
                logger.error("Exception while parsing json payload", ex)
            }
        }

        return result
    }

    override fun get(key: String?): ByteArray? {
        var result: ByteArray? = null

        if (key != null)
            try {
                result = Decryptor().execute(encryptedPayload, key, iv)
            } catch (ex: Exception) {
                logger.error("Exception while decrypting payload", ex)
            }

        return result
    }

    companion object {
        fun toEncryptedJsonPayload(id: String, obj: Any, key: String): EncryptedPayload {
            val e = Encryptor()
            val iv = e.generateIVBase64()
            val json = om.writeValueAsBytes(obj)

            return EncryptedPayload(
                id = id,
                iv = iv,
                encryptedPayload = e.execute(json, key, iv)
            )
        }

        fun toEncryptedPayload(id: String, data: ByteArray, key: String): EncryptedPayload {
            val e = Encryptor()
            val iv = e.generateIVBase64()

            return EncryptedPayload(
                id = id,
                iv = iv,
                encryptedPayload = e.execute(data, key, iv)
            )
        }
    }
}
