package com.yourssu.scouter.auth.authentication.business.dto

data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
)
