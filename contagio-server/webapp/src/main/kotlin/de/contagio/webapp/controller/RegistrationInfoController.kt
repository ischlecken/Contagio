@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.port.IFindAllRegistrationInfo
import de.contagio.core.domain.port.PageRequest
import de.contagio.core.domain.port.SortDirection
import de.contagio.core.domain.port.Sorting
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class RegistrationInfoController(
    private val findAllRegistrationInfo: IFindAllRegistrationInfo,
    private val urlBuilder: UrlBuilder
) {
    @GetMapping("/registrationinfo")
    open fun teststation(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "registrationinfo")
        model.addAttribute(
            "registrationInfo",
            findAllRegistrationInfo.execute(
                PageRequest(
                    pageNo = pageable.pageNumber,
                    pageSize = pageable.pageSize,
                    sort = listOf( Sorting("created", SortDirection.desc))
                )
            )
        )
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("PASS", urlBuilder.passURL),
                Breadcrumb("REGISTRATIONINFO", urlBuilder.registrationinfoURL, true),
            )
        )

        return "registrationinfo"
    }

}
