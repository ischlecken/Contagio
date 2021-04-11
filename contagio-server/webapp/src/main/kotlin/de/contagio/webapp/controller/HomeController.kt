@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.webapp.service.ContagioInfoService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class HomeController(
    private val contagioInfoService: ContagioInfoService
) {

    @GetMapping("/")
    open fun home(model: Model): String {

        model.addAttribute("activePassCount", contagioInfoService.activePassCount)
        model.addAttribute("pageType", "home")

        return "home"
    }


}
