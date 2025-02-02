package com.yourssu.scouter.hrms.business.domain.member

data class UpdateActiveMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val membershipFee: Boolean? = null,
)
