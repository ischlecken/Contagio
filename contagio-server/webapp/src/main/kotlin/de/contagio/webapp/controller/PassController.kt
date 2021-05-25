@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.IFindAllPassInfoEnvelope
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.IdType
import de.contagio.core.usecase.*
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
    private val urlBuilder: UrlBuilder,
    private val getEncryptionKey: IGetEncryptionKey,
    private val deletePass: DeletePass,
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    private val updateOnlyPassInfoEnvelope: UpdateOnlyPassInfoEnvelope,
    private val passCommandProcessor: PassCommandProcessor
) {

    @GetMapping("/pass")
    open fun home(model: Model, pageable: Pageable): String {

        val passes = findAllPassInfoEnvelope.execute(
            pageable.toPageRequest().copy(sort = defaultSort)
        )

        val unlockedSerialNumbers = mutableListOf<String>()
        passes.content.forEach { pie ->
            getEncryptionKey.execute(IdType.SERIALNUMBER, pie.serialNumber)?.apply {
                unlockedSerialNumbers.add(pie.serialNumber)
            }
        }

        model.addAttribute("pageType", "pass")
        model.addAttribute("passInfo", passes)
        model.addAttribute("unlockedSerialNumbers", unlockedSerialNumbers)
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
            "delete" -> passCommandProcessor.addCommand(
                DeletePassCommand(notifyAllDevicesWithInstalledSerialNumber, deletePass, serialnumber)
            )
            "expire" -> passCommandProcessor.addCommand(
                ExpirePassCommand(
                    notifyAllDevicesWithInstalledSerialNumber,
                    updateOnlyPassInfoEnvelope,
                    updatePass,
                    serialnumber
                )
            )
            "revoke" -> passCommandProcessor.addCommand(
                RevokePassCommand(
                    notifyAllDevicesWithInstalledSerialNumber, updateOnlyPassInfoEnvelope,
                    updatePass, serialnumber
                )
            )
            "issue" -> passCommandProcessor.addCommand(
                IssuePassCommand(
                    notifyAllDevicesWithInstalledSerialNumber, updateOnlyPassInfoEnvelope,
                    updatePass, serialnumber
                )
            )
            "negative" -> passCommandProcessor.addCommand(
                NegativePassCommand(
                    notifyAllDevicesWithInstalledSerialNumber,
                    updateOnlyPassInfoEnvelope,
                    updatePass,
                    serialnumber
                )
            )
            "positive" -> passCommandProcessor.addCommand(
                PositivePassCommand(
                    notifyAllDevicesWithInstalledSerialNumber,
                    updateOnlyPassInfoEnvelope,
                    updatePass,
                    serialnumber
                )
            )
        }

        return "redirect:/pass"
    }
}
