package com.yourssu.scouter.auth.authentication.business

data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
)
