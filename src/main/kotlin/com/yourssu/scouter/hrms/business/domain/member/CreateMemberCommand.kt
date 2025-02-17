package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateMemberCommand(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate,
    val departmentId: Long,
    val studentId: String,
    val parts: List<Long> = listOf(),
    val role: MemberRole,
    val nicknameEnglish: String,
    val nicknameKorean: String,
    val state: MemberState,
    val joinDate: LocalDate,
    val note: String,
) {

    fun toDomain(department: Department, parts: List<Part>): Member = Member(
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        birthDate = birthDate,
        department = department,
        studentId = studentId,
        parts = parts.toSortedSet(),
        role = role,
        nicknameEnglish = nicknameEnglish,
        nicknameKorean = nicknameKorean,
        state = state,
        joinDate = joinDate,
        stateUpdatedTime = LocalDateTime.now(),
        note = note,
    )
}
