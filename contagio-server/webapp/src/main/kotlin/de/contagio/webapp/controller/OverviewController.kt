@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.entity.TestResultType
import de.contagio.webapp.repository.mongodb.PassImageRepository
import de.contagio.webapp.repository.mongodb.PassInfoRepository
import de.contagio.webapp.repository.mongodb.PassRepository
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
open class OverviewController(
    private val passInfoRepository: PassInfoRepository,
    private val passImageRepository: PassImageRepository,
    private val passRepository: PassRepository,
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


    @PostMapping("/overview")
    open fun commands(
        @RequestParam serialnumber: String,
        @RequestParam command: String
    ): String {

        passInfoRepository.findById(serialnumber).ifPresent { passInfo ->
            when (command) {
                "delete" -> {
                    passInfo.passId?.let { passId ->
                        passRepository.deleteById(passId)
                    }

                    passImageRepository.deleteById(passInfo.imageId)
                    passInfoRepository.deleteById(passInfo.serialNumber)
                }
                "revoke" -> {
                    passInfoRepository.save(
                        passInfo.copy(
                            issueStatus = IssueStatus.REVOKED,
                            modified = LocalDateTime.now()
                        )
                    )
                }
                "negative" -> {
                    passInfoRepository.save(
                        passInfo.copy(
                            testResult = TestResultType.NEGATIVE,
                            issueStatus = IssueStatus.SIGNED,
                            modified = LocalDateTime.now()
                        )
                    )
                }
                "positive" -> {
                    passInfoRepository.save(
                        passInfo.copy(
                            testResult = TestResultType.POSITIVE,
                            issueStatus = IssueStatus.SIGNED,
                            modified = LocalDateTime.now()
                        )
                    )
                }
            }
        }

        return "redirect:overview"
    }
}
