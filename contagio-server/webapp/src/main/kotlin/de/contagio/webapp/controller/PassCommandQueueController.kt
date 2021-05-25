@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.service.PassCommandProcessor
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class PassCommandQueueController(
    private val passCommandProcessor: PassCommandProcessor,
    private val urlBuilder: UrlBuilder
) {

    @GetMapping("/passcommandqueue")
    open fun home(model: Model, pageable: Pageable): String {

        val commandQueue = passCommandProcessor.getCommands(pageable)

        model.addAttribute("pageType", "passcommandqueue")
        model.addAttribute("passcommandqueue", commandQueue)

        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("PASSCOMMANDQUEUE", urlBuilder.passCommandQueueURL, true),
            )
        )

        return "passcommandqueue"
    }


}
