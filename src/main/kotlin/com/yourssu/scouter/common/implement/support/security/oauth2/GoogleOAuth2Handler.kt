package com.yourssu.scouter.common.implement.support.security.oauth2

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Handler
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2TokenInfo
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2User
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2UserInfo
import java.util.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder

@Component
class GoogleOAuth2Handler(
    val googleOAuth2Properties: GoogleOAuth2Properties,
    val googleOAuth2TokenClient: GoogleOAuth2TokenClient,
    val googleUserApiClient: GoogleUserApiClient,
) : OAuth2Handler {

    override fun getSupportingOAuth2Type() = OAuth2Type.GOOGLE

    override fun provideAuthCodeRequestUrl(): String {
        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/auth")
            .queryParam("client_id", googleOAuth2Properties.clientId)
            .queryParam("redirect_uri", googleOAuth2Properties.redirectUri)
            .queryParam("response_type", "code")
            .queryParam("scope", googleOAuth2Properties.scope.joinToString(" "))
            .queryParam("access_type", "offline")
            .queryParam("prompt", "consent")
            .build()
            .toUriString()
    }

    override fun fetchOAuth2User(authorizationCode: String): OAuth2User {
        val tokenInfo: OAuth2TokenInfo = fetchTokenInfo(authorizationCode)

        val authorizationHeaderValue = StringBuilder()
            .append(tokenInfo.tokenPrefix)
            .append(" ")
            .append(tokenInfo.accessToken)
            .toString()

        val userInfo: OAuth2UserInfo = fetchUserInfo(authorizationHeaderValue)

        return OAuth2User(userInfo, tokenInfo)
    }

    private fun fetchTokenInfo(authorizationCode: String): OAuth2TokenInfo {
        val tokenRequest = LinkedMultiValueMap<String, String>().apply {
            add("client_id", googleOAuth2Properties.clientId)
            add("client_secret", googleOAuth2Properties.clientSecret)
            add("code", authorizationCode)
            add("grant_type", "authorization_code")
            add("redirect_uri", googleOAuth2Properties.redirectUri)
        }

        val tokenResponse: GoogleTokenResponse = googleOAuth2TokenClient.fetchToken(tokenRequest)

        return OAuth2TokenInfo(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            tokenPrefix = tokenResponse.tokenType,
            expiresIn = tokenResponse.expiresIn,
        )
    }

    override fun refreshAccessToken(refreshToken: String): OAuth2TokenInfo {
        val refreshRequest = LinkedMultiValueMap<String, String>().apply {
            add("client_id", googleOAuth2Properties.clientId)
            add("client_secret", googleOAuth2Properties.clientSecret)
            add("refresh_token", refreshToken)
            add("grant_type", "refresh_token")
        }

        val tokenResponse: GoogleTokenResponse = googleOAuth2TokenClient.fetchToken(refreshRequest)

        return OAuth2TokenInfo(
            accessToken = tokenResponse.accessToken,
            tokenPrefix = tokenResponse.tokenType,
            expiresIn = tokenResponse.expiresIn,
        )
    }

    private fun fetchUserInfo(typeSpecifiedAccessToken: String): OAuth2UserInfo {
        val userInfoResponse = googleUserApiClient.fetchUserInfo(typeSpecifiedAccessToken)

        val name: String =
            userInfoResponse.name
                ?: userInfoResponse.givenName
                ?: userInfoResponse.familyName
                ?: UUID.randomUUID().toString()
        return OAuth2UserInfo(
            oauthId = userInfoResponse.id,
            oauth2Type = getSupportingOAuth2Type(),
            name = name,
            email = userInfoResponse.email,
            profileImageUrl = userInfoResponse.picture ?: "",
        )
    }
}
