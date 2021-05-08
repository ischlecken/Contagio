@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.usecase.SearchPassInfo
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class ShowPassController(
    private val searchPassInfo: SearchPassInfo,
    private val urlBuilder: UrlBuilder
) {

    @GetMapping("/verify")
    open fun verify(
        model: Model,
        @RequestParam serialNumber: String,
        @RequestParam showDetails: Boolean?
    ) = searchPassInfo
        .execute(serialNumber)?.let {
            model.addAttribute("pageType", "verify")
            model.addAttribute("extendedPassInfo", it)
            model.addAttribute("showDetails", showDetails ?: false)

            "verify"
        } ?: "redirect:/pass"

    @GetMapping("/showpass")
    open fun showpass(
        model: Model,
        @RequestParam serialNumber: String,
        @RequestParam showDetails: Boolean?
    ) = searchPassInfo
        .execute(serialNumber)?.let {
            model.addAttribute("pageType", "showpass")
            model.addAttribute("extendedPassInfo", it)
            model.addAttribute("showDetails", showDetails ?: false)
            model.addAttribute(
                "breadcrumbinfo",
                listOf(
                    Breadcrumb("HOME", urlBuilder.homeURL),
                    Breadcrumb("PASS", urlBuilder.passURL),
                    Breadcrumb("SHOWPASS", urlBuilder.showpassURL, true),
                )
            )

            "showpass"
        } ?: "redirect:/pass"

}
