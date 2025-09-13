package com.yourssu.scouter.common.application.domain.authentication

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.yourssu.scouter.common.business.domain.authentication.LoginResult
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
class SwaggerOAuth2Controller(private val oauth2Service: OAuth2Service) {

    @Value("\${springdoc.swagger-ui.oauth2-redirect-url}")
    private lateinit var redirectUri: String

    @PostMapping("/oauth2/swagger/callback")
    fun callback(
        @RequestParam code: String,
        @RequestParam(required = false) error: String?
    ): ResponseEntity<AuthResponse> {
        if (error != null) throw RuntimeException(
            "OAuth2 authorization code flow failed: $error"
        )

        val referer = redirectUri.substringBefore("/")
        val loginResult: LoginResult = oauth2Service.login(
            oauth2Type = OAuth2Type.GOOGLE, // 인증, 인가 로직을 시험하기 위함이 아니기 때문에 google로 고정해뒀습니다.
            oauth2AuthorizationCode = code,
            referer = referer,
            redirectUriOverride = redirectUri,
        )
        val accessToken = loginResult.accessToken.substringAfter(" ")
        return ResponseEntity.ok(AuthResponse(accessToken))
    }
}

@JsonNaming(SnakeCaseStrategy::class)
data class AuthResponse(val accessToken: String)


