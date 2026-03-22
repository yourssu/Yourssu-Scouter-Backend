package com.yourssu.scouter.hrms.business.domain.member

import java.time.LocalDate

data class UpdateWithdrawnMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val withdrawnDate: LocalDate? = null,
)
