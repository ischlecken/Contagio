@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.webapp.repository.mongodb.PassInfoRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
open class OverviewController(
    private val passInfoRepository: PassInfoRepository
) {

    @GetMapping("/overview")
    open fun home(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "overview")
        model.addAttribute(
            "passInfo",
            passInfoRepository.findAll(
                PageRequest.of(
                    pageable.pageNumber,
                    pageable.pageSize,
                    Sort.by(Sort.Direction.DESC, "modified", "created")
                )
            )
        )

        return "overview"
    }


}
