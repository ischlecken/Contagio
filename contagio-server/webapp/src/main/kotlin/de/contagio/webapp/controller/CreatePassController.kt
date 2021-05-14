@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.entity.PassType
import de.contagio.core.domain.entity.TestResultType
import de.contagio.core.domain.entity.TestType
import de.contagio.core.usecase.ListOfAllTesterInfo
import de.contagio.core.usecase.SearchTesterWithTeststation
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.restcontroller.pkpassMediatype
import de.contagio.webapp.service.PassService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class CreatePassController(
    private val passService: PassService,
    private val listOfAllTesterInfo: ListOfAllTesterInfo,
    private val searchTesterWithTeststation: SearchTesterWithTeststation,
    private val urlBuilder: UrlBuilder
) {

    @GetMapping("/createpass")
    open fun createPass(model: Model): String {
        model.addAttribute("pageType", "createpass")
        model.addAttribute("testerInfo", listOfAllTesterInfo.execute())

        model.addAttribute("testResultType", TestResultType.values())
        model.addAttribute("testType", TestType.values())
        model.addAttribute("issueStatus", IssueStatus.values())
        model.addAttribute("passType", PassType.values())
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("PASS", urlBuilder.passURL),
                Breadcrumb("CREATEPASS", urlBuilder.createpassURL, true),
            )
        )
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
            headers.add("Location", "/pass")

            return ResponseEntity(headers, HttpStatus.FOUND)
        }

        val teststationId = searchTesterWithTeststation
            .execute(testerId)?.teststation?.id ?: return ResponseEntity.badRequest().build()

        return when (command) {
            "preview" -> {
                val cpr = passService.createPass(
                    image,
                    firstName, lastName,
                    phoneNo, email,
                    teststationId, testerId,
                    testResult, testType,
                    passType,
                    labelColor, foregroundColor, backgroundColor,
                    save = false
                )

                if (cpr?.pass?.isNotEmpty() == true)
                    ResponseEntity
                        .ok()
                        .header("Content-Disposition", "attachment; filename=\"preview.pkpass\"")
                        .contentType(pkpassMediatype)
                        .body(cpr.pass)
                else
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
            "create" -> {
                val cpr = passService.createPass(
                    image,
                    firstName, lastName,
                    phoneNo, email,
                    teststationId, testerId,
                    testResult, testType,
                    passType,
                    labelColor, foregroundColor, backgroundColor,
                    save = true
                )

                val headers = HttpHeaders()
                headers.add("Location", if (cpr?.pkPass != null) "/pass" else "/createpass")

                ResponseEntity(headers, HttpStatus.FOUND)
            }
            else -> ResponseEntity.badRequest().build()
        }
    }
}
