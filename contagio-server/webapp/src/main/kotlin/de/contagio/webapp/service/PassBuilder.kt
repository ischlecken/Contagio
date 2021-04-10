package de.contagio.webapp.service

import de.contagio.core.domain.entity.Pass
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.usecase.CreatePass
import de.contagio.webapp.model.CreatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassImageRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import org.springframework.stereotype.Service

@Service
class PassBuilder(
    private val passInfoRepository: PassInfoRepository,
    private val passImageRepository: PassImageRepository,
    private val passRepository: PassRepository,
    private val contagioProperties: ContagioProperties
) {

    fun build(createPassRequest: CreatePassRequest): PassInfo? {
        val createPass = CreatePass(
            teamIdentifier = contagioProperties.teamIdentifier,
            passTypeIdentifier = contagioProperties.passTypeId,
            authenticationToken = createPassRequest.passInfo.authToken,
            baseUrl = "https://efeu.local:13013"
        )

        val pkpass = createPass.buildSignedPassPayload(
            contagioProperties.passResourcesDir,
            contagioProperties.keyName,
            contagioProperties.privateKeyPassword,
            contagioProperties.templateName,
            createPass.build(createPassRequest.passInfo, createPassRequest.passImage)
        )

        return if (pkpass != null) {
            val pass = Pass(id = createPassRequest.passInfo.passId, data = pkpass)

            passRepository.save(pass)
            passImageRepository.save(createPassRequest.passImage)

            passInfoRepository.save(createPassRequest.passInfo)
        } else
            null
    }
}
