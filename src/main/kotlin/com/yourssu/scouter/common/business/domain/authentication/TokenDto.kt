package com.yourssu.scouter.common.business.domain.authentication

data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
)
