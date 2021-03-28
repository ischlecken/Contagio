@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class HomeController {


    @GetMapping("/")
    open fun home(
        @RequestParam view: String?,
        model: Model
    ): String {

        return "home"
    }



}
