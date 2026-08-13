package com.yourssu.scouter.member.core.business.dto

import com.yourssu.scouter.member.core.business.dto.MemberDto

data class MeResult(
    val profileImageUrl: String,
    val member: MemberDto,
)
