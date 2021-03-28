package de.contagio.webapp.controller

import de.contagio.webapp.config.BuildInfoConfig
import de.contagio.webapp.model.PageMetainfo
import de.contagio.webapp.model.properties.ContagioProperties
import de.contagio.webapp.service.MessageTemplateServiceIF
import org.springframework.beans.factory.annotation.Value
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute


@ControllerAdvice
open class GlobalControllerAdvice(
    private val messageTemplateService: MessageTemplateServiceIF,
    private val buildConfig: BuildInfoConfig,
    private val contagioProperties: ContagioProperties,
    @Value("\${spring.application.name}")
    private val appName: String,
) {

    @ModelAttribute
    fun addToModel(model: Model) {
        model.addAttribute("buildInfo", buildConfig.buildInfo)
        model.addAttribute("appName", appName)

        if (contagioProperties.useMinifiedResources && buildConfig.buildInfo.revision.length == 5) {
            model.addAttribute("useMinifiedResources", true)
            model.addAttribute("minifiedResourceId", buildConfig.buildInfo.revision)
        } else
            model.addAttribute("useMinifiedResources", false)

        model.addAttribute(
            "pageMetainfo",
            PageMetainfo(
                title = messageTemplateService.getString("pagetitle"),
                robots = "index,follow,noarchive"
            )
        )
    }
}
