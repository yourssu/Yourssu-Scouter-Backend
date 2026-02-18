package com.yourssu.scouter.common.implement.domain.authentication

data class Token(
    val accessToken: String,
    val refreshToken: String,
)
