package com.yourssu.scouter.common.implement.support.security.oauth2

import com.yourssu.scouter.common.implement.domain.authentication.*
import com.yourssu.scouter.common.implement.support.exception.CustomException
import feign.FeignException
import org.springframework.http.HttpStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@Component
class GoogleOAuth2Handler(
    val googleOAuth2Properties: GoogleOAuth2Properties,
    val googleOAuth2TokenClient: GoogleOAuth2TokenClient,
    val googleUserApiClient: GoogleUserApiClient,
) : OAuth2Handler {

    private val logger = LoggerFactory.getLogger(GoogleOAuth2Handler::class.java)

    override fun getSupportingOAuth2Type() = OAuth2Type.GOOGLE

    override fun provideAuthCodeRequestUrl(referer: String): String {
        val redirectUri = resolveRedirectUri(referer, null)
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

    override fun fetchOAuth2User(authorizationCode: String, referer: String, redirectUriOverride: String?): OAuth2User {
        val tokenInfo: OAuth2TokenInfo = fetchTokenInfo(authorizationCode, referer, redirectUriOverride)

        val authorizationHeaderValue = StringBuilder()
            .append(tokenInfo.tokenPrefix)
            .append(" ")
            .append(tokenInfo.accessToken)
            .toString()

        val userInfo: OAuth2UserInfo = fetchUserInfo(authorizationHeaderValue)

        return OAuth2User(userInfo, tokenInfo)
    }

    private fun fetchTokenInfo(authorizationCode: String, referer: String, redirectUriOverride: String?): OAuth2TokenInfo {
        val redirectUri = resolveRedirectUri(referer, redirectUriOverride)
        logger.info(">>> [GoogleOAuth2Handler] using redirect_uri={}", redirectUri)

        val tokenRequest = LinkedMultiValueMap<String, String>().apply {
            add("client_id", googleOAuth2Properties.clientId)
            add("client_secret", googleOAuth2Properties.clientSecret)
            add("code", authorizationCode)
            add("grant_type", "authorization_code")
            add("redirect_uri", redirectUri)
        }

        val tokenResponse: GoogleTokenResponse = try {
            googleOAuth2TokenClient.fetchToken(tokenRequest)
        } catch (e: FeignException) {
            val status = e.status()
            val body = try { e.contentUTF8() } catch (_: Throwable) { null }
            logger.error(
                ">>> [GoogleOAuth2Handler] token exchange failed: status={}, redirect_uri={}, body={}",
                status, redirectUri, body
            )
            val mappedStatus = if (status == 401) HttpStatus.UNAUTHORIZED else HttpStatus.BAD_REQUEST
            throw CustomException(
                message = "OAuth2 토큰 교환 실패(${status})",
                errorCode = "OAuth-Token-Exchange-Fail",
                status = mappedStatus
            )
        }

        return OAuth2TokenInfo(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            tokenPrefix = tokenResponse.tokenType,
            expiresIn = tokenResponse.expiresIn,
        )
    }

    private fun selectAllowedRedirectUri(candidate: String?): String? {
        if (candidate.isNullOrBlank()) return null
        val trimmed = candidate.trim()
        val allowList = googleOAuth2Properties.allowedRedirectUris ?: emptyList()
        val ok = allowList.any { allowed -> trimmed.equals(allowed, ignoreCase = true) }
        return if (ok) trimmed else null
    }

    override fun refreshAccessToken(refreshToken: String): OAuth2TokenInfo {
        val refreshRequest = LinkedMultiValueMap<String, String>().apply {
            add("client_id", googleOAuth2Properties.clientId)
            add("client_secret", googleOAuth2Properties.clientSecret)
            add("refresh_token", refreshToken)
            add("grant_type", "refresh_token")
        }

        val tokenResponse: GoogleTokenResponse = try {
            googleOAuth2TokenClient.fetchToken(refreshRequest)
        } catch (e: FeignException) {
            val status = e.status()
            val body = try { e.contentUTF8() } catch (_: Throwable) { null }
            logger.error(
                ">>> [GoogleOAuth2Handler] refresh token failed: status={}, body={}",
                status, body
            )
            val mappedStatus = if (status == 401) HttpStatus.UNAUTHORIZED else HttpStatus.BAD_REQUEST
            throw CustomException(
                message = "OAuth2 토큰 갱신 실패(${status})",
                errorCode = "OAuth-Token-Refresh-Fail",
                status = mappedStatus
            )
        }

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

    private fun resolveRedirectUri(referer: String, redirectUriOverride: String?): String {
        val fromOverride = selectAllowedRedirectUri(redirectUriOverride)
        return fromOverride ?: (googleOAuth2Properties.redirectUri
            ?: googleOAuth2Properties.calculateRedirectUri(referer))
    }
}
