package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.business.domain.department.DepartmentDto
import com.yourssu.scouter.common.business.domain.part.PartDto
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import java.time.LocalDate
import java.time.LocalDateTime

data class MemberDto(
    val id: Long,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate,
    val department: DepartmentDto,
    val studentId: String,
    val parts: List<PartDto>,
    val role: MemberRole,
    val nicknameEnglish: String,
    val nicknameKorean: String,
    val state: MemberState,
    val joinDate: LocalDate,
    val note: String,
    val stateUpdatedTime: LocalDateTime,
    val createdTime: LocalDateTime,
    val updatedTime: LocalDateTime,
) {

    companion object {
        fun from(member: Member): MemberDto = MemberDto(
            id = member.id!!,
            name = member.name,
            email = member.email,
            phoneNumber = member.phoneNumber,
            birthDate = member.birthDate,
            department = DepartmentDto.from(member.department),
            studentId = member.studentId,
            parts = member.parts.map { PartDto.from(it) },
            role = member.role,
            nicknameEnglish = member.nicknameEnglish,
            nicknameKorean = member.nicknameKorean,
            state = member.state,
            joinDate = member.joinDate,
            note = member.note,
            stateUpdatedTime = member.stateUpdatedTime,
            createdTime = member.createdTime!!,
            updatedTime = member.updatedTime!!,
        )
    }
}
