package com.yourssu.scouter.hrms.business.domain.member

data class UpdateWithdrawnMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
)
