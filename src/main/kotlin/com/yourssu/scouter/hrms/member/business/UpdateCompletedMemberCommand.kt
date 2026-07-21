package com.yourssu.scouter.hrms.member.business

data class UpdateCompletedMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val completionSemester: String? = null,
)
