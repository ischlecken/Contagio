@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.webapp.repository.mongodb.PassInfoRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class ShowPassController(
    private val passInfoRepository: PassInfoRepository
) {

    @GetMapping("/showpass")
    open fun home(
        model: Model,
        @RequestParam serialNumber: String
    ): String {

        val passInfo = passInfoRepository.findById(serialNumber)

        if (passInfo.isPresent) {
            model.addAttribute("pageType", "showpass")
            model.addAttribute("passInfo",passInfo.get())

            return "showpass"
        }

        return "redirect:/overview"
    }


}
