package com.yourssu.scouter.hrms.member.business.dto

data class UpdateCompletedMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val completionSemester: String? = null,
)
