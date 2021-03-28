package de.contagio.webapp.controller

import org.springframework.http.HttpStatus
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

fun goneView(location: String): RedirectView {
    val result = RedirectView(location)

    result.setStatusCode(HttpStatus.GONE)

    return result
}

fun permanentMovedView(location: String): RedirectView {
    val result = RedirectView(location)

    result.setStatusCode(HttpStatus.MOVED_PERMANENTLY)

    return result
}

fun temporaryMovedView(location: String): RedirectView {
    val result = RedirectView(location)

    result.setStatusCode(HttpStatus.FOUND)

    return result
}

fun permanentMovedModelAndView(location: String) =
        ModelAndView(permanentMovedView(location))
