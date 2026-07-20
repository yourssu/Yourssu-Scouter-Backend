package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember
import java.time.LocalDate

data class WithdrawnMemberDto(
    val id: Long,
    override val member: MemberDto,
    val withdrawnDate: LocalDate? = null,
) : MemberStateDto {

    companion object {
        fun from(withdrawnMember: WithdrawnMember): WithdrawnMemberDto = WithdrawnMemberDto(
            id = withdrawnMember.id!!,
            member = MemberDto.from(withdrawnMember.member),
            withdrawnDate = withdrawnMember.withdrawnDate,
        )
    }
}
