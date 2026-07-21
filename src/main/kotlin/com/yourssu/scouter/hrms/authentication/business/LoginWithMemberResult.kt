package com.yourssu.scouter.hrms.authentication.business

import com.yourssu.scouter.hrms.member.business.dto.MemberDto

data class LoginWithMemberResult(
    val accessToken: String,
    val refreshToken: String,
    val profileImageUrl: String,
    val member: MemberDto,
)
