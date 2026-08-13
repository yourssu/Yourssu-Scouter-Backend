package com.yourssu.scouter.member.core.business.dto

import com.yourssu.scouter.member.core.implement.ActiveMember

data class ActiveMemberDto(
    val id: Long,
    val member: MemberDto,
    val isMembershipFeePaid: Boolean,
    val grade: Int? = null,
    val isOnLeave: Boolean? = null,
) {

    companion object {
        fun from(activeMember: ActiveMember): ActiveMemberDto = ActiveMemberDto(
            id = activeMember.id!!,
            member = MemberDto.from(activeMember.member),
            isMembershipFeePaid = activeMember.isMembershipFeePaid,
            grade = activeMember.grade,
            isOnLeave = activeMember.isOnLeave,
        )
    }
}
