package com.yourssu.scouter.member.core.business.dto

data class UpdateCompletedMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val completionSemester: String? = null,
)
