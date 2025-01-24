package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import java.time.LocalDate

data class CreateMemberCommand(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate,
    val departmentId: Long,
    val studentId: String,
    val partId: Long,
    val role: MemberRole,
    val nicknameEnglish: String,
    val nicknameKorean: String,
    val state: MemberState,
    val joinDate: LocalDate,
    val isMembershipFeePaid: Boolean,
    val note: String,
) {

    fun toDomain(department: Department, part: Part): Member = Member(
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        birthDate = birthDate,
        department = department,
        studentId = studentId,
        part = part,
        role = role,
        nicknameEnglish = nicknameEnglish,
        nicknameKorean = nicknameKorean,
        state = state,
        joinDate = joinDate,
        isMembershipFeePaid = isMembershipFeePaid,
        note = note,
    )
}
