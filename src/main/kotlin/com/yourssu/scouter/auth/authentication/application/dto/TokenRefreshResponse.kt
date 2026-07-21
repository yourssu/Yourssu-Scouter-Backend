package com.yourssu.scouter.auth.authentication.application.dto

import com.yourssu.scouter.auth.authentication.business.dto.TokenDto

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
