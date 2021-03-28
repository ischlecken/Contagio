package de.contagio.webapp.controller

import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.stereotype.Controller

@Controller
class AppErrorController(
    errorAttributes: ErrorAttributes,
    errorProperties: ErrorProperties
) : BasicErrorController(errorAttributes, errorProperties)
