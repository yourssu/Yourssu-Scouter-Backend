package com.yourssu.scouter.common.implement.domain.authentication

data class OAuth2User(
    val userInfo: OAuth2UserInfo,
    val token: OAuth2TokenInfo,
)

data class OAuth2UserInfo(
    val oauthId: String,
    val oauth2Type: OAuth2Type,
    val name: String,
    val email: String,
    val profileImageUrl: String,
)

data class OAuth2TokenInfo(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenPrefix: String,
    val expiresIn: Long,
)
