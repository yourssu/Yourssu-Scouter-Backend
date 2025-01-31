package com.yourssu.scouter.common.implement.support.security.oauth2

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Handler
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder

@Component
class GoogleOAuth2Handler(
    val googleOAuth2Properties: GoogleOAuth2Properties
) : OAuth2Handler {

    override fun getSupportingOAuth2Type() = OAuth2Type.GOOGLE

    override fun provideAuthCodeRequestUrl(): String {
        val redirectUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(googleOAuth2Properties.relativeRedirectUri).toUriString()

        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/auth")
            .queryParam("client_id", googleOAuth2Properties.clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .queryParam("scope", googleOAuth2Properties.scope.joinToString(" "))
            .queryParam("access_type", "offline")
            .queryParam("prompt", "consent")
            .build()
            .toUriString()
    }
}
