@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.PassType
import de.contagio.core.domain.entity.TestResultType
import de.contagio.core.domain.entity.TestType
import de.contagio.core.domain.port.IFindAllTester
import de.contagio.core.domain.port.IFindAllTeststation
import de.contagio.core.usecase.ListOfAllTesterInfo
import de.contagio.webapp.repository.mongodb.TesterRepository
import de.contagio.webapp.repository.mongodb.TeststationRepository
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
    private val findAllTeststation: IFindAllTeststation,
    private val findAllTester: IFindAllTester,
    private val testerRepository: TesterRepository,
    private val teststationRepository: TeststationRepository
) {

    @GetMapping("/createpass")
    open fun createPass(model: Model): String {
        val listOfAllTesterInfo = ListOfAllTesterInfo(findAllTeststation, findAllTester)

        model.addAttribute("pageType", "createpass")
        model.addAttribute("testerInfo", listOfAllTesterInfo.execute())

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
            headers.add("Location", "/overview")

            return ResponseEntity(headers, HttpStatus.FOUND)
        }

        var teststationId: String? = null
        val tester = testerRepository.findById(testerId)
        if (tester.isPresent) {
            teststationRepository.findById(tester.get().teststationId).ifPresent {
                teststationId = it.id
            }
        }

        if (teststationId == null)
            return ResponseEntity.badRequest().build()

        return when (command) {
            "preview" -> {
                val cpr = passService.createPass(
                    image,
                    firstName, lastName,
                    phoneNo, email,
                    teststationId!!, testerId,
                    testResult, testType,
                    passType,
                    labelColor, foregroundColor, backgroundColor
                )

                if (cpr.pkPass != null && cpr.pkPass.isNotEmpty())
                    ResponseEntity
                        .ok()
                        .header("Content-Disposition", "attachment; filename=\"preview.pkpass\"")
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
                    teststationId!!, testerId,
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
