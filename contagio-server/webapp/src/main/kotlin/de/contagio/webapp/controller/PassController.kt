@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.port.IFindAllPassInfoEnvelope
import de.contagio.core.domain.port.IFindUpdatePassRequest
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.IdType
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.service.PassCommandProcessor
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
    private val findAllPassInfoEnvelope: IFindAllPassInfoEnvelope,
    private val findUpdatePassRequest: IFindUpdatePassRequest,
    private val urlBuilder: UrlBuilder,
    private val getEncryptionKey: IGetEncryptionKey,
    private val passCommandProcessor: PassCommandProcessor
) {

    @GetMapping("/pass")
    open fun home(model: Model, pageable: Pageable): String {

        val passes = findAllPassInfoEnvelope.execute(
            pageable.toPageRequest().copy(sort = defaultSort)
        )

        val unlockedSerialNumbers = mutableListOf<String>()
        val pendingUpdates = mutableListOf<String>()
        val cssClasses = mutableMapOf<String,String>()
        passes.content.forEach { p ->
            var cssClass = ""
            getEncryptionKey.execute(IdType.SERIALNUMBER, p.serialNumber)?.apply {
                unlockedSerialNumbers.add(p.serialNumber)

                cssClass += " unlocked"
            }

            findUpdatePassRequest.execute(p.serialNumber)?.apply {
                pendingUpdates.add(p.serialNumber)

                cssClass += " pending"
            }

            cssClasses[p.serialNumber] = cssClass
        }

        model.addAttribute("pageType", "pass")
        model.addAttribute("passInfo", passes)
        model.addAttribute("unlockedSerialNumbers", unlockedSerialNumbers)
        model.addAttribute("pendingUpdates", pendingUpdates)
        model.addAttribute("cssClasses", cssClasses)
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("PASS", urlBuilder.passURL, true),
            )
        )

        if (passCommandProcessor.isProcessing)
            model.addAttribute("refreshPage", true)

        return "pass"
    }


    @PostMapping("/pass")
    open fun commands(
        @RequestParam serialnumber: String,
        @RequestParam command: String
    ): String {

        when (command) {
            "delete" -> passCommandProcessor.deletePass(serialnumber)
            "expire" -> passCommandProcessor.expirePass(serialnumber)
            "revoke" -> passCommandProcessor.revokePass(serialnumber)
            "negative" -> passCommandProcessor.negativeTestresult(serialnumber)
            "positive" -> passCommandProcessor.positiveTestresult(serialnumber)
        }

        return "redirect:/pass"
    }
}
