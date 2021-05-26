package de.contagio.webapp.service

import de.contagio.core.usecase.SignatureBuilder
import de.contagio.webapp.model.properties.ContagioProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
open class SignDataService(
    private val contagioProperties: ContagioProperties,
) {

    @Value("classpath:certs/contagio-sign.p12")
    private lateinit var contagioSignKeystore: Resource

    open fun sign(data: ByteArray) =
        SignatureBuilder(
            contagioSignKeystore.inputStream,
            contagioProperties.sign.password,
            contagioProperties.sign.keyname
        ).execute(data)

}
