package com.yourssu.scouter.common.application.support.configuration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val method = request.method
        val uri = request.requestURI
        if (!method.equals("OPTIONS", ignoreCase = true)) {
            val referer = request.getHeader("Referer")
            val origin = request.getHeader("Origin")
            val auth = request.getHeader("Authorization")
            val authPreview = auth?.let { if (it.length > 12) it.substring(0, 12) + "..." else it } ?: "<none>"
            log.info("[ACCESS] {} {} | origin={} | referer={} | auth={}", method, uri, origin, referer, authPreview)
        }

        filterChain.doFilter(request, response)
    }
}
