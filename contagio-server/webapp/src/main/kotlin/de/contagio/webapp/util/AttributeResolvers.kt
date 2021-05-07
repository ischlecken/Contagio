package de.contagio.webapp.util

import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.entity.Teststation
import de.contagio.core.usecase.SearchTesterWithTeststation
import de.contagio.core.usecase.SearchTeststation
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.http.HttpServletRequest


@Suppress("UNCHECKED_CAST")
fun NativeWebRequest.pathVariables(): Map<String, String> {
    val nativeWebRequest = this.getNativeRequest(HttpServletRequest::class.java)!!

    return nativeWebRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, String>
}

class TesterAttributeResolver(
    private val searchTesterWithTeststation: SearchTesterWithTeststation
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Tester::class.java
    }

    @Throws(Exception::class)
    override fun resolveArgument(
        parameter: MethodParameter?,
        mvContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        var testerid = webRequest.getParameter("testerid")

        if (testerid == null)
            testerid = webRequest.pathVariables()["testerid"]

        return testerid?.let {
            searchTesterWithTeststation.execute(it)?.tester
        }
    }
}


class TeststationAttributeResolver(
    private val searchTeststation: SearchTeststation
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Teststation::class.java
    }

    @Throws(Exception::class)
    override fun resolveArgument(
        parameter: MethodParameter?,
        mvContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        var teststationid = webRequest.getParameter("teststationid")

        if (teststationid == null)
            teststationid = webRequest.pathVariables()["teststationid"]

        return teststationid?.let {
            searchTeststation.execute(it)
        }
    }

}
