package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.AuthenticationService
import com.yourssu.scouter.common.business.domain.authentication.LoginResult
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.business.domain.authentication.TokenDto
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
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

@RestController
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val oauth2Service: OAuth2Service,
) {

    private val logger = LoggerFactory.getLogger(AuthenticationController::class.java)

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

    @PostMapping("/logout")
    fun logout(
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody @Valid request: LogoutRequest,
    ): ResponseEntity<Unit> {
        authenticationService.logout(accessToken, request.refreshToken)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/validate-token")
    fun validateToken(
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
    ): ResponseEntity<ValidateTokenResponse> {
        val validated: Boolean = authenticationService.isValidToken(TokenType.ACCESS, accessToken)
        val response = ValidateTokenResponse(validated)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh-token")
    fun refreshToken(
        @RequestBody @Valid request: TokenRefreshRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) bearerRefreshHeader: String?,
    ): ResponseEntity<TokenRefreshResponse> {
        logger.info("[Auth] POST /refresh-token | hasAuthHeader={} | bodyPresent={}", !bearerRefreshHeader.isNullOrBlank(), !request.refreshToken.isNullOrBlank())
        val requestTime = LocalDateTime.now()
        val providedRefreshToken = if (!bearerRefreshHeader.isNullOrBlank()) bearerRefreshHeader else request.refreshToken
        val tokenDto: TokenDto = authenticationService.refreshToken(requestTime, providedRefreshToken)
        val response: TokenRefreshResponse = TokenRefreshResponse.from(tokenDto)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/withdrawal")
    fun withdraw(
        @RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String,
        @RequestBody @Valid request: WithdrawalRequest,
    ): ResponseEntity<Unit> {
        authenticationService.withdraw(accessToken, request.refreshToken)

        return ResponseEntity.noContent().build()
    }
}
