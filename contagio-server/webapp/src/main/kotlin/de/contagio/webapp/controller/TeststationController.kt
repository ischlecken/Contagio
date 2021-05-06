@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.repository.mongodb.TeststationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class TeststationController(
    private val teststationRepository: TeststationRepository,
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
                Breadcrumb("OVERVIEW", urlBuilder.overviewURL),
                Breadcrumb("TESTSTATION", urlBuilder.teststationURL, true),
            )
        )

        return "teststation"
    }


    @PostMapping("/teststation")
    open fun commands(
        @RequestParam teststationid: String,
        @RequestParam command: String
    ): String {

        teststationRepository.findById(teststationid).ifPresent { teststation ->
            when (command) {
                "delete" -> {
                    teststationRepository.deleteById(teststation.id)
                }
            }
        }

        return "redirect:teststation"
    }
}
