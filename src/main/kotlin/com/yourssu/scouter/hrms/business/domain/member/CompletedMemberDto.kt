package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.CompletedMember

data class CompletedMemberDto(
    val id: Long,
    val member: MemberDto,
    val activePeriod: SemesterPeriodDto,
    val isAdvisorDesired: Boolean = false,
) {

    companion object {
        fun from(completedMember: CompletedMember): CompletedMemberDto = CompletedMemberDto(
            id = completedMember.id!!,
            member = MemberDto.from(completedMember.member),
            activePeriod = SemesterPeriodDto.from(completedMember.activePeriod),
            isAdvisorDesired = completedMember.isAdvisorDesired,
        )
    }
}
