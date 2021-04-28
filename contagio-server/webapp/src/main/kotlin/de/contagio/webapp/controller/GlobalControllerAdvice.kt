package de.contagio.webapp.controller

import de.contagio.webapp.config.BuildInfoConfig
import de.contagio.webapp.model.PageMetainfo
import de.contagio.webapp.service.MessageTemplateServiceIF
import org.springframework.beans.factory.annotation.Value
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute


@ControllerAdvice
open class GlobalControllerAdvice(
    private val messageTemplateService: MessageTemplateServiceIF,
    private val buildConfig: BuildInfoConfig,
    @Value("\${spring.application.name}")
    private val appName: String,
) {

    @ModelAttribute
    fun addToModel(model: Model) {
        model.addAttribute("buildInfo", buildConfig.buildInfo)
        model.addAttribute("appName", appName)

        model.addAttribute(
            "pageMetainfo",
            PageMetainfo(
                title = messageTemplateService.getString("pagetitle"),
                home = "https://www.til.de",
                imprint = "TIL (c) 2021",
                robots = "noindex, nofollow, noarchive"
            )
        )
    }
}
