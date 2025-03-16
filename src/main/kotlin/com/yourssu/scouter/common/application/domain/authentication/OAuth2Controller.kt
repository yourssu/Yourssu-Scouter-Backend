package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class OAuth2Controller(
    private val oauth2Service: OAuth2Service
) {

    @GetMapping("/oauth2/{oauth2Type}")
    fun redirectAuthCodeRequestUrl(
        @PathVariable oauth2Type: OAuth2Type,
        response: HttpServletResponse,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<Unit> {
        val redirectUrl: String = oauth2Service.getAuthCodeRequestUrl(
            oauth2Type = oauth2Type,
            referer = httpServletRequest.getHeader(HttpHeaders.REFERER),
        )

        response.sendRedirect(redirectUrl)

        return ResponseEntity.ok().build()
    }
}
