package com.yourssu.scouter.hrms.member.business.dto

import com.yourssu.scouter.hrms.member.implement.WithdrawnMember
import java.time.LocalDate

data class WithdrawnMemberDto(
    val id: Long,
    val member: MemberDto,
    val withdrawnDate: LocalDate? = null,
) {

    companion object {
        fun from(withdrawnMember: WithdrawnMember): WithdrawnMemberDto = WithdrawnMemberDto(
            id = withdrawnMember.id!!,
            member = MemberDto.from(withdrawnMember.member),
            withdrawnDate = withdrawnMember.withdrawnDate,
        )
    }
}
