@file:Suppress("SpringMVCViewInspection")

package de.contagio.webapp.controller

import de.contagio.core.domain.entity.Address
import de.contagio.core.domain.entity.Teststation
import de.contagio.core.usecase.UrlBuilder
import de.contagio.core.util.UIDGenerator
import de.contagio.webapp.model.Breadcrumb
import de.contagio.webapp.repository.mongodb.TeststationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore
import java.time.Instant

@ApiIgnore
@Controller
open class TeststationController(
    private val teststationRepository: TeststationRepository,
    private val urlBuilder: UrlBuilder
) {

    private val uidGenerator = UIDGenerator()

    @GetMapping("/teststation")
    open fun teststation(
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
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("TESTSTATION", urlBuilder.teststationURL, true),
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

        return "redirect:/teststation"
    }

    @GetMapping("/createteststation")
    open fun createTeststation(model: Model): String {
        model.addAttribute("pageType", "createteststation")
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("TESTSTATION", urlBuilder.teststationURL),
                Breadcrumb("CREATETESTSTATION", urlBuilder.createteststationURL, true),
            )
        )

        return "createteststation"
    }

    @PostMapping("/createteststation")
    open fun createTeststation(
        @RequestParam name: String,
        @RequestParam zipcode: String,
        @RequestParam city: String,
        @RequestParam street: String,
        @RequestParam hno: String,
    ): String {

        teststationRepository.save(
            Teststation(
                id = uidGenerator.generate(),
                name = name,
                address = Address(city = city, zipcode = zipcode, street = street, hno = hno)
            )
        )


        return "redirect:/teststation"
    }

    @GetMapping("/editteststation/{id}")
    open fun editTeststation(model: Model, @PathVariable id: String): String {
        model.addAttribute("pageType", "editteststation")
        model.addAttribute(
            "breadcrumbinfo",
            listOf(
                Breadcrumb("HOME", urlBuilder.homeURL),
                Breadcrumb("TESTSTATION", urlBuilder.teststationURL),
                Breadcrumb("EDITTESTSTATION", urlBuilder.editteststationURL, true),
            )
        )

        teststationRepository.findById(id).ifPresent {
            model.addAttribute("teststation", it)
        }

        return "editteststation"
    }

    @PostMapping("/editteststation")
    open fun editTeststation(
        @RequestParam command: String,
        @RequestParam id: String,
        @RequestParam name: String,
        @RequestParam zipcode: String,
        @RequestParam city: String,
        @RequestParam street: String,
        @RequestParam hno: String,
    ): String {

        teststationRepository.findById(id).ifPresent {
            when (command) {
                "delete" -> teststationRepository.deleteById(id)
                "save" -> teststationRepository.save(
                    it.copy(
                        name = name,
                        address = Address(city = city, zipcode = zipcode, street = street, hno = hno),
                        modified = Instant.now()
                    )
                )
            }
        }

        return "redirect:/teststation"
    }
}
