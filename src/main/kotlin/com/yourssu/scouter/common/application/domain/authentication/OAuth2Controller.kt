package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.application.support.exception.ExceptionResponse
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증/인가")
@Tag(name = "OAUTH2")
@RestController
class OAuth2Controller(
    private val oauth2Service: OAuth2Service
) {

    @Operation(
        summary = "Google refresh token 유효 여부 조회",
        description = "현재 사용자의 Google OAuth2 refresh token이 유효한지 검사합니다. " +
            "무효일 때는 401을 반환하며, 다른 인증 실패(401)와 동일하게 재로그인 유도 처리하면 됩니다. " +
            "메일 예약, 구글 폼/드라이브 동기화 등 Google API 연동 기능에서 공통으로 사용할 수 있습니다.",
    )
    @ApiResponse(responseCode = "200", description = "유효함")
    @ApiResponse(responseCode = "401", description = "Google 토큰 무효 — 재로그인 필요 (body.errorCode: OAuth-Token-Refresh-Fail)")
    @GetMapping("/oauth2/google-refresh-token-status")
    fun getGoogleRefreshTokenStatus(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<Any> {
        val result = oauth2Service.checkGoogleRefreshTokenValidity(authUserInfo.userId)
        return if (result.valid) {
            ResponseEntity.ok(OAuth2RefreshTokenStatusResponse.from(result))
        } else {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                    ExceptionResponse(
                        HttpStatus.UNAUTHORIZED,
                        result.errorCode ?: "OAuth-Token-Refresh-Fail",
                        "OAuth2 토큰 갱신 실패. 재로그인이 필요합니다.",
                    ),
                )
        }
    }

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
