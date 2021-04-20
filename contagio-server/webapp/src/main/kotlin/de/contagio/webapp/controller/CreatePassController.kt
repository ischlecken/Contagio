@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.*
import de.contagio.webapp.restcontroller.pkpassMediatype
import de.contagio.webapp.service.PassService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class CreatePassController(private val passService: PassService) {

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
        @RequestParam testType: TestType,
        @RequestParam passType: PassType,
        @RequestParam templateName: String?
    ): ResponseEntity<ByteArray> {

        val cpr = passService.createPass(
            image,
            firstName,
            lastName,
            phoneNo,
            email,
            teststationId,
            testerId,
            testResult,
            testType,
            passType,
            templateName
        )

        return if (cpr != null)
            ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=\"${cpr.passInfo.passId}.pkpass\"")
                .contentType(pkpassMediatype)
                .body(cpr.pkPass)
        else
            ResponseEntity.badRequest().build()
    }
}
