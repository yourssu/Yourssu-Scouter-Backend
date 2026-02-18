package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember

data class InactiveMemberDto(
    val id: Long,
    val member: MemberDto,
    val activePeriod: SemesterPeriodDto,
    val expectedReturnSemester: SemesterDto,
    val inactivePeriod: SemesterPeriodDto,
) {

    companion object {
        fun from(inactiveMember: InactiveMember): InactiveMemberDto = InactiveMemberDto(
            id = inactiveMember.id!!,
            member = MemberDto.from(inactiveMember.member),
            activePeriod = SemesterPeriodDto.from(inactiveMember.activePeriod),
            expectedReturnSemester = SemesterDto.from(inactiveMember.expectedReturnSemester),
            inactivePeriod = SemesterPeriodDto.from(inactiveMember.inactivePeriod),
        )
    }
}
