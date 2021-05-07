@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.CreateTesterDTO
import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.entity.UpdateTesterDTO
import de.contagio.core.domain.port.IDeleteTester
import de.contagio.core.domain.port.IFindAllTeststation
import de.contagio.core.usecase.CreateTester
import de.contagio.core.usecase.UpdateTester
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.repository.mongodb.TesterRepository
import de.contagio.webapp.service.validate.ValidateTester
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class TesterController(
    private val testerRepository: TesterRepository,
    private val createTester: CreateTester,
    private val updateTester: UpdateTester,
    private val deleteTester: IDeleteTester,
    private val findAllTeststation: IFindAllTeststation,
    private val urlBuilder: UrlBuilder
) {

    @GetMapping("/tester")
    open fun tester(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "tester")
        model.addAttribute(
            "tester",
            testerRepository.findAll(
                PageRequest.of(
                    pageable.pageNumber,
                    pageable.pageSize,
                    Sort.by(Sort.Direction.DESC, "created")
                )
            )
        )
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("OVERVIEW", urlBuilder.overviewURL),
                Breadcrumb("TESTER", urlBuilder.testerURL, true),
            )
        )

        return "tester"
    }


    @GetMapping("/createtester")
    open fun createTester(model: Model): String {
        model.addAttribute("pageType", "createtester")
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("TESTER", urlBuilder.testerURL),
                Breadcrumb("CREATETESTER", urlBuilder.createtesterURL, true),
            )
        )
        model.addAttribute("createTesterDTO", CreateTesterDTO())
        model.addAttribute("teststationInfo", findAllTeststation.execute())

        return "createtester"
    }

    @PostMapping("/createtester")
    open fun createTester(
        @ModelAttribute createTesterDTO: CreateTesterDTO
    ): String {
        createTester.execute(createTesterDTO)

        return "redirect:/tester"
    }

    @ValidateTester
    @GetMapping("/edittester/{testerid}")
    open fun editTester(
        @PathVariable testerid: String,
        model: Model,
        tester: Tester
    ): String {

        model.addAttribute("pageType", "edittester")
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("TESTER", urlBuilder.testerURL),
                Breadcrumb("EDITTESTER", urlBuilder.edittesterURL, true),
            )
        )

        model.addAttribute("testerid", testerid)
        model.addAttribute(
            "updateTesterDTO",
            UpdateTesterDTO(
                firstName = tester.person.firstName,
                lastName = tester.person.lastName,
                phoneNo = tester.person.phoneNo ?: "",
                email = tester.person.email ?: ""
            )
        )

        return "edittester"
    }

    @ValidateTester
    @PostMapping("/edittester")
    open fun editTester(
        @RequestParam command: String,
        @RequestParam testerid: String,
        @ModelAttribute updateTesterDTO: UpdateTesterDTO,
        tester: Tester
    ): String {

        when (command) {
            "delete" -> deleteTester.execute(tester)
            "save" -> updateTester.execute(tester, updateTesterDTO)
        }

        return "redirect:/tester"
    }
}
