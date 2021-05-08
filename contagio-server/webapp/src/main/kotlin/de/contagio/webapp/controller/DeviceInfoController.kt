@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.port.*
import de.contagio.core.usecase.UrlBuilder
import de.contagio.webapp.model.Breadcrumb
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class DeviceInfoController(
    private val findAllDeviceInfo: IFindAllDeviceInfo,
    private val urlBuilder: UrlBuilder
) {
    @GetMapping("/deviceinfo")
    open fun deviceinfo(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "deviceinfo")
        model.addAttribute(
            "deviceInfo",
            findAllDeviceInfo.execute(
                PageRequest(
                    pageNo = pageable.pageNumber,
                    pageSize = pageable.pageSize,
                    sort = listOf(Sorting("created", SortDirection.desc))
                )
            )
        )
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("PASS", urlBuilder.passURL),
                Breadcrumb("REGISTRATIONINFO", urlBuilder.registrationinfoURL),
                Breadcrumb("DEVICEINFO", urlBuilder.deviceinfoURL, true),
            )
        )

        return "deviceinfo"
    }

}
