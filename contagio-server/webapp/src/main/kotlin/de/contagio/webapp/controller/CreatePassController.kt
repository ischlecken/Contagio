@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.brendamour.jpasskit.util.CertUtils
import de.contagio.core.domain.entity.PassType
import de.contagio.core.domain.entity.TestResultType
import de.contagio.core.domain.entity.TestType
import de.contagio.webapp.restcontroller.pkpassMediatype
import de.contagio.webapp.service.PassService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import springfox.documentation.annotations.ApiIgnore


private var logger = LoggerFactory.getLogger(CreatePassController::class.java)

@ApiIgnore
@Controller
open class CreatePassController(private val passService: PassService) {

    @Value("classpath:certs/pass.p12")
    private lateinit var passKeystore: Resource

    @GetMapping("/createpass")
    open fun createPass(): String {
        logger.debug("createPass()")
        logger.debug("  passKeystore=${passKeystore.url} ${passKeystore.filename}")

        val passUrl = CertUtils::class.java.classLoader.getResource("certs/pass.p12")
        logger.debug("  passUrl=${passUrl}")

        return "createpass"
    }

    @PostMapping("/createpass")
    open fun createPass(
        @RequestParam command: String,
        @RequestParam image: MultipartFile,
        @RequestParam firstName: String,
        @RequestParam lastName: String,
        @RequestParam phoneNo: String,
        @RequestParam email: String?,
        @RequestParam teststationId: String,
        @RequestParam testerId: String,
        @RequestParam testResult: TestResultType,
        @RequestParam testType: TestType,
        @RequestParam passType: PassType,
        @RequestParam labelColor: String,
        @RequestParam foregroundColor: String,
        @RequestParam backgroundColor: String
    ): ResponseEntity<ByteArray> {

        if (command.isEmpty() || !(command == "preview" || command == "create")) {
            val headers = HttpHeaders()
            headers.add("Location", "/overview")

            return ResponseEntity(headers, HttpStatus.FOUND)
        }

        return when (command) {
            "preview" -> {
                val cpr = passService.createPass(
                    image,
                    firstName, lastName,
                    phoneNo, email,
                    teststationId, testerId,
                    testResult, testType,
                    passType,
                    labelColor, foregroundColor, backgroundColor
                )

                if (cpr.pkPass != null && cpr.pkPass.isNotEmpty())
                    ResponseEntity
                        .ok()
                        .header("Content-Disposition", "attachment; filename=\"${cpr.passInfo.passId}.pkpass\"")
                        .contentType(pkpassMediatype)
                        .body(cpr.pkPass)
                else
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
            "create" -> {
                val cpr = passService.createPassAndSave(
                    image,
                    firstName, lastName,
                    phoneNo, email,
                    teststationId, testerId,
                    testResult, testType,
                    passType,
                    labelColor, foregroundColor, backgroundColor
                )

                val headers = HttpHeaders()
                headers.add(
                    "Location",
                    if (cpr.pkPass != null) "/overview" else "/createpass"
                )

                ResponseEntity(headers, HttpStatus.FOUND)
            }
            else -> ResponseEntity.badRequest().build()
        }
    }
}
