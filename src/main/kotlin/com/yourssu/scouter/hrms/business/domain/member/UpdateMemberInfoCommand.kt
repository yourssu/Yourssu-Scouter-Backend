package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import java.time.LocalDate

data class UpdateMemberInfoCommand(
    val targetMemberId: Long,
    val partIds: List<Long>? = null,
    val role: MemberRole? = null,
    val name: String? = null,
    val nicknameEnglish: String? = null,
    val nicknameKorean: String? = null,
    val state: MemberState? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val departmentId: Long? = null,
    val studentId: String? = null,
    val birthDate: LocalDate? = null,
    val joinDate: LocalDate? = null,
    val note: String? = null,
)
