package com.yourssu.scouter.hrms.business.domain.member

data class UpdateActiveMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val isMembershipFeePaid: Boolean? = null,
    val grade: Int? = null,
    val isOnLeave: Boolean? = null,
)
