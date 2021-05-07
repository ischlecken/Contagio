@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.CreateTeststationDTO
import de.contagio.core.domain.entity.Teststation
import de.contagio.core.domain.entity.UpdateTeststationDTO
import de.contagio.core.domain.port.IDeleteTeststation
import de.contagio.core.usecase.CreateTeststation
import de.contagio.core.usecase.UpdateTeststation
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.repository.mongodb.TeststationRepository
import de.contagio.webapp.service.validate.ValidateTeststation
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class TeststationController(
    private val teststationRepository: TeststationRepository,
    private val updateTeststation: UpdateTeststation,
    private val deleteTeststation: IDeleteTeststation,
    private val createTeststation: CreateTeststation,
    private val urlBuilder: UrlBuilder
) {
    @GetMapping("/teststation")
    open fun teststation(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "teststation")
        model.addAttribute(
            "teststation",
            teststationRepository.findAll(
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
                Breadcrumb("TESTSTATION", urlBuilder.teststationURL, true),
            )
        )

        return "teststation"
    }

    @ValidateTeststation
    @PostMapping("/teststation")
    open fun commands(
        @RequestParam teststationid: String,
        @RequestParam command: String,
        teststation: Teststation
    ): String {

        when (command) {
            "delete" -> {
                deleteTeststation.execute(teststation)
            }
        }

        return "redirect:/teststation"
    }

    @GetMapping("/createteststation")
    open fun createTeststation(model: Model): String {
        model.addAttribute("pageType", "createteststation")
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("TESTSTATION", urlBuilder.teststationURL),
                Breadcrumb("CREATETESTSTATION", urlBuilder.createteststationURL, true),
            )
        )
        model.addAttribute("createTeststationDTO", CreateTeststationDTO())

        return "createteststation"
    }

    @PostMapping("/createteststation")
    open fun createTeststation(
        @ModelAttribute createTeststationDTO: CreateTeststationDTO
    ): String {
        createTeststation.execute(createTeststationDTO)

        return "redirect:/teststation"
    }

    @ValidateTeststation
    @GetMapping("/editteststation/{teststationid}")
    open fun editTeststation(
        @PathVariable teststationid: String,
        model: Model,
        teststation: Teststation
    ): String {

        model.addAttribute("pageType", "editteststation")
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("TESTSTATION", urlBuilder.teststationURL),
                Breadcrumb("EDITTESTSTATION", urlBuilder.editteststationURL, true),
            )
        )

        model.addAttribute("teststationid", teststationid)
        model.addAttribute(
            "updateTeststationDTO",
            UpdateTeststationDTO(
                teststation.name,
                teststation.address.zipcode,
                teststation.address.city,
                teststation.address.street ?: "",
                teststation.address.hno ?: ""
            )
        )

        return "editteststation"
    }

    @ValidateTeststation
    @PostMapping("/editteststation")
    open fun editTeststation(
        @RequestParam command: String,
        @RequestParam teststationid: String,
        @ModelAttribute updateTeststationDTO: UpdateTeststationDTO,
        teststation: Teststation
    ): String {

        when (command) {
            "delete" -> deleteTeststation.execute(teststation)
            "save" -> updateTeststation.execute(teststation, updateTeststationDTO)
        }

        return "redirect:/teststation"
    }
}
