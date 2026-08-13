package com.yourssu.scouter.auth.login.business.dto

import com.yourssu.scouter.member.core.business.dto.MemberDto

data class LoginWithMemberResult(
    val accessToken: String,
    val refreshToken: String,
    val profileImageUrl: String,
    val member: MemberDto,
)
