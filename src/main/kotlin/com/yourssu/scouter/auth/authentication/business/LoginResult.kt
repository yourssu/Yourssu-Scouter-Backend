package com.yourssu.scouter.auth.authentication.business

data class LoginResult(
    val id: Long,
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val profileImageUrl: String,
)
