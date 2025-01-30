package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember

data class WithdrawnMemberDto(
    val id: Long,
    val member: MemberDto,
) {

    companion object {
        fun from(withdrawnMember: WithdrawnMember): WithdrawnMemberDto = WithdrawnMemberDto(
            id = withdrawnMember.id!!,
            member = MemberDto.from(withdrawnMember.member),
        )
    }
}
