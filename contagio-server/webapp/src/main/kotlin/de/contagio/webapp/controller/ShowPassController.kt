@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.usecase.SearchPassInfo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class ShowPassController(
    private val searchPassInfo: SearchPassInfo
) {

    @GetMapping("/showpass")
    open fun home(
        model: Model,
        @RequestParam serialNumber: String,
        @RequestParam showDetails: Boolean?
    ) = searchPassInfo
        .execute(serialNumber)?.let {
            model.addAttribute("pageType", "showpass")
            model.addAttribute("extendedPassInfo", it)
            model.addAttribute("showDetails", showDetails ?: false)

            "showpass"
        } ?: "redirect:/overview"

}
