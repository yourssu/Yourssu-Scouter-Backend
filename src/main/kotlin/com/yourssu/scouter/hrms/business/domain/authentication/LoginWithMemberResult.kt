package com.yourssu.scouter.hrms.business.domain.authentication

import com.yourssu.scouter.hrms.business.domain.member.MemberDto

data class LoginWithMemberResult(
    val accessToken: String,
    val refreshToken: String,
    val profileImageUrl: String,
    val member: MemberDto,
)
