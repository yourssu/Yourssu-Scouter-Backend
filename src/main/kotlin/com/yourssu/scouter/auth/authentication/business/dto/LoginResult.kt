package com.yourssu.scouter.auth.authentication.business.dto

data class LoginResult(
    val id: Long,
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val profileImageUrl: String,
)
