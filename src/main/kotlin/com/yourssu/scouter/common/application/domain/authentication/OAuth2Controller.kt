package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@Tag(name = "인증/인가")
@Tag(name = "OAUTH2")
@RestController
class OAuth2Controller(
    private val oauth2Service: OAuth2Service
) {

    @Operation(summary = "OAuth2 로그인 페이지 리다이렉트", description = "여기에서는 리다이렉트가 되지 않습니다. Authorize 이용.")
    @SecurityRequirements // 로그인을 필요로 하지 않는 곳은 전역 인증을 사용하지않도록 초기화
    @GetMapping("/oauth2/{oauth2Type}")
    fun redirectAuthCodeRequestUrl(
        @PathVariable oauth2Type: OAuth2Type,
        response: HttpServletResponse,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<Unit> {
        val referer = httpServletRequest.getHeader(HttpHeaders.REFERER)
            ?: "http://localhost:8080"

        val redirectUrl: String = oauth2Service.getAuthCodeRequestUrl(
            oauth2Type = oauth2Type,
            referer = referer,
        )

        response.sendRedirect(redirectUrl)

        return ResponseEntity.ok().build()
    }
}
