package com.yourssu.scouter.member.core.business.dto

import com.yourssu.scouter.masterdata.semester.business.dto.SemesterDto
import com.yourssu.scouter.member.core.implement.CompletedMember

data class CompletedMemberDto(
    val id: Long,
    val member: MemberDto,
    val completionSemester: SemesterDto,
) {

    companion object {
        fun from(completedMember: CompletedMember): CompletedMemberDto = CompletedMemberDto(
            id = completedMember.id!!,
            member = MemberDto.from(completedMember.member),
            completionSemester = SemesterDto.from(completedMember.completionSemester),
        )
    }
}
