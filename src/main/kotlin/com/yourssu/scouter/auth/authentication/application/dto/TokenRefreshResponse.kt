package com.yourssu.scouter.auth.authentication.application.dto

import com.yourssu.scouter.auth.authentication.business.dto.TokenDto
import com.yourssu.scouter.auth.support.constants.AuthConstants

data class TokenRefreshResponse(
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
) {

    companion object {
        fun from(tokenDto: TokenDto) = TokenRefreshResponse(
            tokenType = AuthConstants.BEARER_TOKEN_TYPE,
            accessToken = tokenDto.accessToken,
            refreshToken = tokenDto.refreshToken,
        )
    }
}
