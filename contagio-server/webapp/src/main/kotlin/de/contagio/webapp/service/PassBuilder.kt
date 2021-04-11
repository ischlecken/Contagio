package de.contagio.webapp.service

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.entity.Pass
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.usecase.CreatePass
import de.contagio.core.usecase.CreatePassParameter
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.CreatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassImageRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PassBuilder(
    private val passInfoRepository: PassInfoRepository,
    private val passImageRepository: PassImageRepository,
    private val passRepository: PassRepository,
    private val contagioProperties: ContagioProperties
) {

    private val uidGenerator = UIDGenerator()


    fun build(createPassRequest: CreatePassRequest): PassInfo? {
        val createPass = CreatePass(
            teamIdentifier = contagioProperties.teamIdentifier,
            passTypeIdentifier = contagioProperties.passTypeId,
            authenticationToken = createPassRequest.passInfo.authToken,
            baseUrl = contagioProperties.baseUrl
        )

        var passInfo = createPassRequest.passInfo.copy(
            validUntil = LocalDateTime.now().plusHours(12)
        )

        val createPassParameter = CreatePassParameter(
            organisationName = contagioProperties.passOrganisationName,
            description = contagioProperties.passDescription,
            logoText = contagioProperties.passLogoText,
        )
        val pkpass = createPass.buildSignedPassPayload(
            contagioProperties.passResourcesDir,
            contagioProperties.keyName,
            contagioProperties.privateKeyPassword,
            contagioProperties.templateName,
            createPassRequest.passImage,
            createPass.build(
                passInfo,
                createPassParameter
            )
        )

        return if (pkpass != null) {
            val passInfo = passInfo.copy(
                issueStatus = IssueStatus.SIGNED,
                passId = uidGenerator.generate()
            )

            passRepository.save(Pass(id = passInfo.passId!!, data = pkpass))
            passImageRepository.save(createPassRequest.passImage)
            passInfoRepository.save(passInfo)
        } else {
            passImageRepository.save(createPassRequest.passImage)
            passInfoRepository.save(createPassRequest.passInfo.copy(issueStatus = IssueStatus.REFUSED))
        }
    }
}
