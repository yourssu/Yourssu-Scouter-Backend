package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.hrms.business.domain.authentication.LoginWithMemberResult

data class LoginResponse(
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
    val profileImageUrl: String,
    val member: LoginMemberResponse,
) {
    companion object {
        fun from(result: LoginWithMemberResult): LoginResponse = LoginResponse(
            tokenType = "Bearer",
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            profileImageUrl = result.profileImageUrl,
            member = LoginMemberResponse.from(result.member),
        )
    }
}
