package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.hrms.business.domain.authentication.LoginWithMemberResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 응답")
data class LoginResponse(
    @field:Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String,
    @field:Schema(description = "액세스 토큰")
    val accessToken: String,
    @field:Schema(description = "리프레시 토큰")
    val refreshToken: String,
) {
    companion object {
        fun from(result: LoginWithMemberResult): LoginResponse = LoginResponse(
            tokenType = "Bearer",
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
        )
    }
}
