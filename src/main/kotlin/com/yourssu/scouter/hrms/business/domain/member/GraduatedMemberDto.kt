package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMember

data class GraduatedMemberDto(
    val id: Long,
    val member: MemberDto,
    val activePeriod: SemesterPeriodDto,
    val isAdvisorDesired: Boolean,
) {

    companion object {
        fun from(graduatedMember: GraduatedMember): GraduatedMemberDto = GraduatedMemberDto(
            id = graduatedMember.id!!,
            member = MemberDto.from(graduatedMember.member),
            activePeriod = SemesterPeriodDto.from(graduatedMember.activePeriod),
            isAdvisorDesired = graduatedMember.isAdvisorDesired,
        )
    }
}
