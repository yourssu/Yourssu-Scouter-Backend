package com.yourssu.scouter.auth.authentication.implement

data class Token(
    val accessToken: String,
    val refreshToken: String,
)
