package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.hrms.implement.domain.member.CompletedMember

data class CompletedMemberDto(
    val id: Long,
    override val member: MemberDto,
    val completionSemester: SemesterDto,
) : MemberStateDto {

    companion object {
        fun from(completedMember: CompletedMember): CompletedMemberDto = CompletedMemberDto(
            id = completedMember.id!!,
            member = MemberDto.from(completedMember.member),
            completionSemester = SemesterDto.from(completedMember.completionSemester),
        )
    }
}
