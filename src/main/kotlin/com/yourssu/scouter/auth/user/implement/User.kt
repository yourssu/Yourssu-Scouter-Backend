package com.yourssu.scouter.auth.user.implement

import com.yourssu.scouter.auth.authentication.implement.OAuth2TokenInfo
import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import java.time.Instant

class User(
    val id: Long? = null,
    val userInfo: UserInfo,
    var tokenInfo: TokenInfo? = null,
) {
    fun getBearerAccessToken(): String {
        return requireTokenInfo().getBearerAccessToken()
    }

    fun getBearerRefreshToken(): String {
        return requireTokenInfo().getBearerRefreshToken()
    }

    fun isAccessTokenRemainMoreThan(minutes: Long): Boolean {
        return requireTokenInfo().isAccessTokenRemainMoreThan(minutes)
    }

    private fun requireTokenInfo(): TokenInfo {
        return tokenInfo ?: throw IllegalStateException("OAuth2 토큰 정보가 없는 사용자입니다.")
    }

    fun updateToken(oauth2TokenInfo: OAuth2TokenInfo) {
        tokenInfo = TokenInfo(
            tokenPrefix = oauth2TokenInfo.tokenPrefix,
            accessToken = oauth2TokenInfo.accessToken,
            refreshToken = oauth2TokenInfo.refreshToken ?: requireTokenInfo().refreshToken,
            accessTokenExpirationDateTime = Instant.now().plusSeconds(oauth2TokenInfo.expiresIn),
        )
    }

    fun getEmailAddress(): String {
        return userInfo.email
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

class UserInfo(
    val name: String,
    val email: String,
    val profileImageUrl: String,
    val authType: AuthType,
    val oauthId: String? = null,
    val oauth2Type: OAuth2Type? = null,
    val password: String? = null,
)

class TokenInfo(
    val tokenPrefix: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpirationDateTime: Instant,
) {
    constructor(tokenPrefix: String, accessToken: String, refreshToken: String, accessTokenExpiresIn: Long) : this(
        tokenPrefix = tokenPrefix,
        accessToken = accessToken,
        refreshToken = refreshToken,
        accessTokenExpirationDateTime = Instant.now().plusSeconds(accessTokenExpiresIn),
    )

    fun isAccessTokenRemainMoreThan(minutes: Long): Boolean {
        return accessTokenExpirationDateTime.minusSeconds(minutes * 60).isAfter(Instant.now())
    }

    fun getBearerAccessToken(): String {
        return StringBuilder()
            .append(tokenPrefix)
            .append(" ")
            .append(accessToken)
            .toString()
    }

    fun getBearerRefreshToken(): String {
        return StringBuilder()
            .append(tokenPrefix)
            .append(" ")
            .append(refreshToken)
            .toString()
    }
}
