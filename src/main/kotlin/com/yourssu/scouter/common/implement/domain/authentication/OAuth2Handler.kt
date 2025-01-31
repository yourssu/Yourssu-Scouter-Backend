package com.yourssu.scouter.common.implement.domain.authentication

interface OAuth2Handler {

    fun getSupportingOAuth2Type(): OAuth2Type
    fun provideAuthCodeRequestUrl(): String
    fun fetchOAuth2User(authorizationCode: String): OAuth2User
}
