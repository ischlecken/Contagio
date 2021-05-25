@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.*
import de.contagio.core.domain.port.IFindAllPassInfoEnvelope
import de.contagio.core.domain.port.IFindAllPassUpdateLog
import de.contagio.core.domain.port.IGetEncryptionKey
import de.contagio.core.domain.port.IdType
import de.contagio.core.usecase.DeletePass
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
open class PassUpdateLogController(
    private val findAllPassUpdateLog: IFindAllPassUpdateLog,
    private val urlBuilder: UrlBuilder
) {

    @GetMapping("/passupdatelog")
    open fun home(model: Model, pageable: Pageable): String {

        val logs = findAllPassUpdateLog.execute(
            pageable.toPageRequest().copy(sort = defaultSort)
        )

        model.addAttribute("pageType", "passupdatelog")
        model.addAttribute("passupdatelogs", logs)

        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("PASSUPDATELOG", urlBuilder.passUpdateLogURL, true),
            )
        )

        return "passupdatelog"
    }


}
