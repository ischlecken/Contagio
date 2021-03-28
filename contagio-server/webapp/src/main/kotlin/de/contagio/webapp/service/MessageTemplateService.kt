package de.contagio.webapp.service

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service
open class MessageTemplateService : MessageTemplateServiceIF {

    override fun getString(templateName: String): String {
        val bundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale())

        return bundle.getString(templateName)
    }

}
