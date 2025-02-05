package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.AuthenticationService
import com.yourssu.scouter.common.business.domain.authentication.LoginResult
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(
    private val authenticationService: AuthenticationService,
) {

    @PostMapping("oauth2/login/{oauth2Type}")
    fun login(
        @PathVariable oauth2Type: OAuth2Type,
        @RequestBody @Valid request: OAuth2LoginRequest,
    ): ResponseEntity<LoginResponse> {

        val loginResult: LoginResult = authenticationService.login(oauth2Type, request.authorizationCode)
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
}
