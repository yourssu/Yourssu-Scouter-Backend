package com.yourssu.scouter.member.core.business.dto

data class UpdateGraduatedMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val isAdvisorDesired: Boolean? = null,
)
