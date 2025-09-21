package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.AuthenticationService
import com.yourssu.scouter.common.business.domain.authentication.LoginResult
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.business.domain.authentication.TokenDto
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import java.time.LocalDateTime
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.slf4j.LoggerFactory

@Tag(name = "인증/인가")
@RestController
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val oauth2Service: OAuth2Service,
) {

    private val logger = LoggerFactory.getLogger(AuthenticationController::class.java)

    @Tag(name = "OAUTH2")
    @Operation(summary = "회원가입/로그인", description = "/oauth2/{oauth2Type}에서 얻은 code를 이용해 회원가입/로그인을 진행합니다.")
    @SecurityRequirements // 로그인을 필요로 하지 않는 곳은 전역 인증을 사용하지않도록 초기화
    @PostMapping("oauth2/login/{oauth2Type}")
    fun login(
        @PathVariable oauth2Type: OAuth2Type,
        @RequestBody @Valid request: OAuth2LoginRequest,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<LoginResponse> {
        val referer = httpServletRequest.getHeader(HttpHeaders.REFERER)
            ?: "http://localhost:8080"
        val redirectUriFromClient = request.redirectUri
        logger.info("[Auth] POST /oauth2/login/{} | referer={} | redirectUri={}", oauth2Type, referer, redirectUriFromClient)

        val loginResult: LoginResult = oauth2Service.login(
            oauth2Type = oauth2Type,
            oauth2AuthorizationCode = request.authorizationCode,
            referer = referer,
            redirectUriOverride = redirectUriFromClient,
        )
        val response: LoginResponse = LoginResponse.from(loginResult)

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "로그아웃")
    @ApiResponse(description = "NO_CONTENT", responseCode = "204")
    @PostMapping("/logout")
    fun logout(
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody @Valid request: LogoutRequest,
    ): ResponseEntity<Unit> {
        authenticationService.logout(accessToken, request.refreshToken)

        return ResponseEntity.noContent().build()
    }

    @SecurityRequirements // 로그인을 필요로 하지 않는 곳은 전역 인증을 사용하지않도록 초기화
    @Operation(summary = "AccessToken 유효성 검사")
    @GetMapping("/validate-token")
    fun validateToken(
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
    ): ResponseEntity<ValidateTokenResponse> {
        val validated: Boolean = authenticationService.isValidToken(TokenType.ACCESS, accessToken)
        val response = ValidateTokenResponse(validated)

        return ResponseEntity.ok(response)
    }

    @SecurityRequirements // 로그인을 필요로 하지 않는 곳은 전역 인증을 사용하지않도록 초기화
    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh-token")
    fun refreshToken(
        @RequestBody @Valid request: TokenRefreshRequest,
    ): ResponseEntity<TokenRefreshResponse> {
        logger.info("[Auth] POST /refresh-token | bodyPresent={} (Authorization header ignored)", !request.refreshToken.isNullOrBlank())
        val requestTime = LocalDateTime.now()
        val tokenDto: TokenDto = authenticationService.refreshToken(requestTime, request.refreshToken)
        val response: TokenRefreshResponse = TokenRefreshResponse.from(tokenDto)

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "회원 탈퇴")
    @ApiResponse(description = "NO_CONTENT", responseCode = "204")
    @PostMapping("/withdrawal")
    fun withdraw(
        @Parameter(hidden = true)
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody @Valid request: WithdrawalRequest,
    ): ResponseEntity<Unit> {
        authenticationService.withdraw(accessToken, request.refreshToken)

        return ResponseEntity.noContent().build()
    }
}
