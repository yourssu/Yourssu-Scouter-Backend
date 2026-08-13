package com.yourssu.scouter.auth.authentication.business.dto

import com.yourssu.scouter.member.core.business.dto.MemberDto

data class LoginWithMemberResult(
    val accessToken: String,
    val refreshToken: String,
    val profileImageUrl: String,
    val member: MemberDto,
)
