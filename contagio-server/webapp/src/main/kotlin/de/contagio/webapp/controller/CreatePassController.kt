@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassInfo
import de.contagio.core.domain.entity.TestResultType
import de.contagio.core.domain.entity.TestType
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.restcontroller.pkpassMediatype
import de.contagio.webapp.service.PassBuilder
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import springfox.documentation.annotations.ApiIgnore
import java.time.LocalDateTime

private var logger = LoggerFactory.getLogger(CreatePassController::class.java)

@ApiIgnore
@Controller
open class CreatePassController(
    private val passBuilder: PassBuilder
) {

    private val uidGenerator = UIDGenerator()

    @GetMapping("/createpass")
    open fun createPass() = "createpass"


    @PostMapping("/createpass")
    open fun createPass(
        @RequestParam image: MultipartFile,
        @RequestParam firstName: String,
        @RequestParam lastName: String,
        @RequestParam phoneNo: String,
        @RequestParam email: String?,
        @RequestParam teststationId: String,
        @RequestParam testerId: String,
        @RequestParam testResult: TestResultType,
        @RequestParam testType: TestType
    ): ResponseEntity<ByteArray> {

        logger.debug("createPass(firstName=$firstName, lastName=$lastName, testResult=$testResult)")
        logger.debug("  image.size=${image.size}")

        val passInfo = PassInfo.build(
            uidGenerator,
            firstName,
            lastName,
            phoneNo,
            email,
            teststationId,
            testerId,
            testResult,
            testType
        ).copy(
            validUntil = LocalDateTime.now().plusMinutes(5)
        )

        val passImage = PassImage.build(image.bytes, image.contentType, passInfo)
        val pkPass = passBuilder.buildPkPass(passInfo, passImage)

        if (pkPass != null)
            return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=\"${passInfo.passId}.pkpass\"")
                .contentType(pkpassMediatype)
                .body(pkPass)

        return ResponseEntity.badRequest().build()
    }
}
