package com.yourssu.scouter.auth.authentication.implement

interface OAuth2Handler {

    fun getSupportingOAuth2Type(): OAuth2Type
    fun provideAuthCodeRequestUrl(referer: String): String
    fun fetchOAuth2User(authorizationCode: String, referer: String, redirectUriOverride: String? = null): OAuth2User
    fun refreshAccessToken(refreshToken: String): OAuth2TokenInfo
}
