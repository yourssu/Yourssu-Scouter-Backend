package com.yourssu.scouter.hrms.business.domain.member

data class UpdateGraduatedMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val isAdvisorDesired: Boolean? = null,
)
