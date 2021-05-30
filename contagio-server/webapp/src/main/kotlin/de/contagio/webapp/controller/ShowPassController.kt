@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.usecase.SearchPassInfo
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.service.PassCommandProcessor
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class ShowPassController(
    private val searchPassInfo: SearchPassInfo,
    private val urlBuilder: UrlBuilder,
    private val passCommandProcessor: PassCommandProcessor
) {

    @GetMapping("/verify")
    open fun verify(
        model: Model,
        @RequestParam serialNumber: String
    ) = searchPassInfo
        .execute(serialNumber)?.let {

            var bodyCssClass = "verify ${it.passInfoEnvelope.issueStatus}"
            if( it.passInfo!=null )
                bodyCssClass += " ${it.passInfo!!.testResult}"

            model.addAttribute("bodyCssClass", bodyCssClass)
            model.addAttribute("pageType", "verify")
            model.addAttribute("refreshPage", it.passInfo == null)
            model.addAttribute("extendedPassInfo", it)

            if( it.passInfo==null ) {
                passCommandProcessor.verifyPass(serialNumber)
            }

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
