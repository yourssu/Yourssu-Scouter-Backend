package com.yourssu.scouter.common.implement.domain.user

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import java.time.LocalDateTime

class User(
    val id: Long? = null,
    val userInfo: UserInfo,
    val tokenInfo: TokenInfo,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "User(id=$id, userInfo=$userInfo, tokenInfo=$tokenInfo)"
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
)
