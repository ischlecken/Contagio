@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.repository.mongodb.TesterRepository
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
open class TesterController(
    private val testerRepository: TesterRepository,
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
                    Sort.by(Sort.Direction.DESC,  "created")
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


    @PostMapping("/tester")
    open fun commands(
        @RequestParam testerid: String,
        @RequestParam command: String
    ): String {

        testerRepository.findById(testerid).ifPresent { tester ->
            when (command) {
                "delete" -> {
                    testerRepository.deleteById(tester.id)
                }
            }
        }

        return "redirect:tester"
    }
}
