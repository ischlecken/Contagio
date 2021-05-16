@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.IDeletePassInfoEnvelope
import de.contagio.core.domain.port.IFindAllPassInfoEnvelope
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.usecase.NotifyAllDevicesWithInstalledSerialNumber
import de.contagio.core.usecase.UpdatePass
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
    private val urlBuilder: UrlBuilder,
    private val getEncryptionKey: IGetEncryptionKey,
    private val deletePassInfoEnvelope: IDeletePassInfoEnvelope,
    private val notifyAllDevicesWithInstalledSerialNumber: NotifyAllDevicesWithInstalledSerialNumber,
    private val updatePass: UpdatePass,
    private val passCommandProcessor: PassCommandProcessor
) {

    @GetMapping("/pass")
    open fun home(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "pass")
        model.addAttribute(
            "passInfo",
            findAllPassInfoEnvelope.execute(
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
            "delete" -> passCommandProcessor.addCommand(
                DeletePassCommand(deletePassInfoEnvelope, serialnumber)
            )
            "expire" -> passCommandProcessor.addCommand(
                ExpirePassCommand(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber, updatePass, serialnumber)
            )
            "revoke" -> passCommandProcessor.addCommand(
                RevokePassCommand(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber, updatePass, serialnumber)
            )
            "issue" -> passCommandProcessor.addCommand(
                IssuePassCommand(getEncryptionKey, notifyAllDevicesWithInstalledSerialNumber, updatePass, serialnumber)
            )
            "negative" -> passCommandProcessor.addCommand(
                NegativePassCommand(
                    getEncryptionKey,
                    notifyAllDevicesWithInstalledSerialNumber,
                    updatePass,
                    serialnumber
                )
            )
            "positive" -> passCommandProcessor.addCommand(
                PositivePassCommand(
                    getEncryptionKey,
                    notifyAllDevicesWithInstalledSerialNumber,
                    updatePass,
                    serialnumber
                )
            )
        }


        return "redirect:/pass"
    }
}
