package com.yourssu.scouter.hrms.member.business.dto

import java.time.LocalDate

data class UpdateWithdrawnMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val withdrawnDate: LocalDate? = null,
)
