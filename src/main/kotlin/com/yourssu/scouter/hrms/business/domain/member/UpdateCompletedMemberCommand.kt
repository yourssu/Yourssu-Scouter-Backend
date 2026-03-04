package com.yourssu.scouter.hrms.business.domain.member

data class UpdateCompletedMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
)
