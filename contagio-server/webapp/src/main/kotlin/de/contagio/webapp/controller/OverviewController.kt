@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.service.PassService
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
open class OverviewController(
    private val passInfoRepository: PassInfoRepository,
    private val passService: PassService,
    private val urlBuilder: UrlBuilder
) {

    @GetMapping("/overview")
    open fun home(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "overview")
        model.addAttribute(
            "passInfo",
            passInfoRepository.findAll(
                PageRequest.of(
                    pageable.pageNumber,
                    pageable.pageSize,
                    Sort.by(Sort.Direction.DESC, "modified", "created")
                )
            )
        )
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("OVERVIEW", urlBuilder.overviewURL, true),
            )
        )


        return "overview"
    }


    @PostMapping("/overview")
    open fun commands(
        @RequestParam serialnumber: String,
        @RequestParam command: String
    ): String {

        when (command) {
            "delete" -> passService.delete(serialnumber)
            "expire" -> passService.expire(serialnumber)
            "revoke" -> passService.revoke(serialnumber)
            "issue" -> passService.issue(serialnumber)
            "negative" -> passService.negative(serialnumber)
            "positive" -> passService.positive(serialnumber)
        }

        return "redirect:overview"
    }
}
