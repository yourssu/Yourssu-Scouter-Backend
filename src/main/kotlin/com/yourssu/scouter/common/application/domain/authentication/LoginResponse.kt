package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.LoginResult

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun from(loginResult: LoginResult): LoginResponse = LoginResponse(
            accessToken = loginResult.accessToken,
            refreshToken = loginResult.refreshToken
        )
    }
}
