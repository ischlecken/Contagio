@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.port.IFindAllPassInfo
import de.contagio.core.domain.port.SortDirection
import de.contagio.core.domain.port.Sorting
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.service.PassService
import de.contagio.webapp.util.defaultSort
import de.contagio.webapp.util.toPageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class PassController(
    private val findAllPassInfo: IFindAllPassInfo,
    private val passService: PassService,
    private val urlBuilder: UrlBuilder
) {

    @GetMapping("/pass")
    open fun home(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "pass")
        model.addAttribute(
            "passInfo",
            findAllPassInfo.execute(
                pageable
                    .toPageRequest()
                    .copy(sort = defaultSort)
            )
        )
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("PASS", urlBuilder.passURL, true),
            )
        )

        return "pass"
    }


    @PostMapping("/pass")
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

        return "redirect:/pass"
    }
}
