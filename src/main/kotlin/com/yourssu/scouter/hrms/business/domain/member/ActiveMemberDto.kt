package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.ActiveMember

data class ActiveMemberDto(
    val id: Long,
    val member: MemberDto,
    val isMembershipFeePaid: Boolean,
) {

    companion object {
        fun from(activeMember: ActiveMember): ActiveMemberDto = ActiveMemberDto(
            id = activeMember.id!!,
            member = MemberDto.from(activeMember.member),
            isMembershipFeePaid = activeMember.isMembershipFeePaid,
        )
    }
}
