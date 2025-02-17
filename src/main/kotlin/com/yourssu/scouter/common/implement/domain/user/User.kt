package com.yourssu.scouter.common.implement.domain.user

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2TokenInfo
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import java.time.LocalDateTime

class User(
    val id: Long? = null,
    val userInfo: UserInfo,
    var tokenInfo: TokenInfo,
) {
    fun getBearerAccessToken(): String {
        return tokenInfo.getBearerAccessToken()
    }

    fun getBearerRefreshToken(): String {
        return tokenInfo.getBearerRefreshToken()
    }

    fun isAccessTokenRemainMoreThan(minutes: Long): Boolean {
        return tokenInfo.isAccessTokenRemainMoreThan(minutes)
    }

    fun updateToken(oauth2TokenInfo: OAuth2TokenInfo) {
        tokenInfo = TokenInfo(
            tokenPrefix = oauth2TokenInfo.tokenPrefix,
            accessToken = oauth2TokenInfo.accessToken,
            refreshToken = oauth2TokenInfo.refreshToken ?: tokenInfo.refreshToken,
            accessTokenExpirationDateTime = LocalDateTime.now().plusSeconds(oauth2TokenInfo.expiresIn),
        )
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
    val oauthId: String,
    val oauth2Type: OAuth2Type,
)

class TokenInfo(
    val tokenPrefix: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpirationDateTime: LocalDateTime,
) {
    constructor(tokenPrefix: String, accessToken: String, refreshToken: String, accessTokenExpiresIn: Long) : this(
        tokenPrefix = tokenPrefix,
        accessToken = accessToken,
        refreshToken = refreshToken,
        accessTokenExpirationDateTime = LocalDateTime.now().plusSeconds(accessTokenExpiresIn),
    )

    fun isAccessTokenRemainMoreThan(minutes: Long): Boolean {
        return accessTokenExpirationDateTime.minusMinutes(minutes).isAfter(LocalDateTime.now())
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
