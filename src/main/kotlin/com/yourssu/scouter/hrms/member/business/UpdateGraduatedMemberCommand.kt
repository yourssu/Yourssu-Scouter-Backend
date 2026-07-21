package com.yourssu.scouter.hrms.member.business

data class UpdateGraduatedMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val isAdvisorDesired: Boolean? = null,
)
