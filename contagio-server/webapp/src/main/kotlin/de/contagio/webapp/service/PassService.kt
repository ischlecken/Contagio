package de.contagio.webapp.service

import de.contagio.core.domain.entity.*
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.CreatePassResponse
import de.contagio.webapp.model.UpdatePassRequest
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.repository.mongodb.PassImageRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import de.contagio.webapp.repository.mongodb.TeststationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime


private var logger = LoggerFactory.getLogger(PassService::class.java)

@Service
open class PassService(
    private val passInfoRepository: PassInfoRepository,
    private val passImageRepository: PassImageRepository,
    private val passRepository: PassRepository,
    private val passBuilder: PassBuilder,
    private val teststationRepository: TeststationRepository,
    private val contagioProperties: ContagioProperties
) {

    private val uidGenerator = UIDGenerator()

    open fun delete(serialnumber: String) {
        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            passInfo.passId?.let { passId ->
                passRepository.deleteById(passId)
            }

            passImageRepository.deleteById(passInfo.imageId)
            passInfoRepository.deleteById(passInfo.serialNumber)
        }
    }

    open fun revoke(serialnumber: String) {
        updateTestResult(serialnumber, TestResultType.UNKNOWN, IssueStatus.REVOKED)
    }

    open fun negative(serialnumber: String) {
        updateTestResult(serialnumber, TestResultType.NEGATIVE, IssueStatus.SIGNED)
    }

    open fun positive(serialnumber: String) {
        updateTestResult(serialnumber, TestResultType.POSITIVE, IssueStatus.SIGNED)
    }

    private fun updateTestResult(
        serialnumber: String,
        testResult: TestResultType,
        issueStatus: IssueStatus
    ): PassInfo? {
        var result: PassInfo? = null

        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            val updatedPassInfo = passInfo.copy(
                testResult = testResult,
                issueStatus = issueStatus,
                modified = LocalDateTime.now(),
                validUntil = LocalDateTime.now().plusDays(if (passInfo.testType == TestType.VACCINATION) 10 else 3),
                version = passInfo.version + 1
            )

            val pass = passRepository.findById(passInfo.passId!!)
            val passImage = passImageRepository.findById(passInfo.imageId)
            val teststation = teststationRepository.findById(passInfo.teststationId)
            if (teststation.isPresent && passImage.isPresent && pass.isPresent) {
                passBuilder.buildPkPass(
                    updatedPassInfo,
                    teststation.get(),
                    passImage.get(),
                    PassType.COUPON
                )?.let {
                    passRepository.save(pass.get().copy(data = it))
                    passInfoRepository.save(updatedPassInfo)

                    result = updatedPassInfo
                }
            }
        }

        return result
    }

    open fun createPass(
        image: MultipartFile,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String?,
        teststationId: String,
        testerId: String,
        testResult: TestResultType,
        testType: TestType,
        passType: PassType
    ): CreatePassResponse {
        logger.debug("createPass(firstName=$firstName, lastName=$lastName, testResult=$testResult)")
        logger.debug("  image.size=${image.size}")

        val passInfo = PassInfo.build(
            uidGenerator,
            firstName, lastName,
            phoneNo, email,
            teststationId, testerId,
            testResult, testType,
        ).copy(
            validUntil = LocalDateTime.now().plusDays(if (testType == TestType.VACCINATION) 10 else 3),
            issueStatus = IssueStatus.SIGNED
        )

        var result = CreatePassResponse(
            passInfo = passInfo,
            passImage = PassImage.build(image.bytes, image.contentType, passInfo)
        )

        val teststation = teststationRepository.findById(teststationId)
        if (teststation.isPresent) {
            passBuilder.buildPkPass(
                passInfo,
                teststation.get(),
                result.passImage,
                passType
            )?.let {
                result = result.copy(pkPass = it)
            }
        }

        return result
    }

    open fun createPassAndSave(
        image: MultipartFile,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String?,
        teststationId: String,
        testerId: String,
        testResult: TestResultType,
        testType: TestType,
        passType: PassType = PassType.COUPON
    ): CreatePassResponse {

        var cpr = createPass(
            image,
            firstName,
            lastName,
            phoneNo,
            email,
            teststationId,
            testerId,
            testResult,
            testType,
            passType
        )

        if (cpr.pkPass != null) {
            cpr = cpr.copy(passInfo = cpr.passInfo.copy(passId = uidGenerator.generate()))

            passRepository.save(Pass(id = cpr.passInfo.passId!!, data = cpr.pkPass!!))
            passImageRepository.save(cpr.passImage)
            passInfoRepository.save(cpr.passInfo)
        } else {
            cpr = cpr.copy(passInfo = cpr.passInfo.copy(issueStatus = IssueStatus.REFUSED))

            passImageRepository.save(cpr.passImage)
            passInfoRepository.save(cpr.passInfo)
        }

        return cpr
    }

    open fun updatePass(updatePassRequest: UpdatePassRequest): PassInfo? {
        val result = passInfoRepository.findById(updatePassRequest.serialNumber)

        if (result.isPresent && updatePassRequest.testResult != null) {
            return updateTestResult(updatePassRequest.serialNumber, updatePassRequest.testResult, IssueStatus.SIGNED)
        }

        return null
    }
}
