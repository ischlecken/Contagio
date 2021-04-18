@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.entity.TestResultType
import de.contagio.webapp.repository.mongodb.PassImageRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
import de.contagio.webapp.repository.mongodb.TeststationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore
import java.time.LocalDateTime

@ApiIgnore
@Controller
open class TeststationController(
    private val teststationRepository: TeststationRepository
) {

    @GetMapping("/teststation")
    open fun home(
        model: Model,
        pageable: Pageable
    ): String {
        model.addAttribute("pageType", "teststation")
        model.addAttribute(
            "teststation",
            teststationRepository.findAll(
                PageRequest.of(
                    pageable.pageNumber,
                    pageable.pageSize,
                    Sort.by(Sort.Direction.DESC, "created")
                )
            )
        )

        return "teststation"
    }


    @PostMapping("/teststation")
    open fun commands(
        @RequestParam teststationid: String,
        @RequestParam command: String
    ): String {

        teststationRepository.findById(teststationid).ifPresent { teststation ->
            when (command) {
                "delete" -> {
                    teststationRepository.deleteById(teststation.id)
                }
            }
        }

        return "redirect:teststation"
    }
}
