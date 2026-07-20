package com.yourssu.scouter.hrms.business.domain.me

import com.yourssu.scouter.hrms.business.domain.member.MemberStateDto

data class MeResult(
    val profileImageUrl: String,
    val member: MemberStateDto,
)
