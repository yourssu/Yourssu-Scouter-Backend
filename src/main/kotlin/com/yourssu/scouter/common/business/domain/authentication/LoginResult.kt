package com.yourssu.scouter.common.business.domain.authentication

data class LoginResult(
    val id: Long,
    val accessToken: String,
    val refreshToken: String,
)
