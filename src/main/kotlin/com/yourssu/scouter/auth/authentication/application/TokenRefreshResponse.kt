package com.yourssu.scouter.auth.authentication.application

import com.yourssu.scouter.auth.authentication.business.TokenDto

data class TokenRefreshResponse(
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
) {

    companion object {
        fun from(tokenDto: TokenDto) = TokenRefreshResponse(
            tokenType = "Bearer",
            accessToken = tokenDto.accessToken,
            refreshToken = tokenDto.refreshToken,
        )
    }
}
