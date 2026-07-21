package com.yourssu.scouter.hrms.me.business.dto

import com.yourssu.scouter.hrms.member.business.dto.MemberDto

data class MeResult(
    val profileImageUrl: String,
    val member: MemberDto,
)
