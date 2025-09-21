package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.LoginResult

data class LoginResponse(
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun from(loginResult: LoginResult): LoginResponse = LoginResponse(
            tokenType = "Bearer",
            accessToken = loginResult.accessToken,
            refreshToken = loginResult.refreshToken
        )
    }
}
