package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import java.time.LocalDate

data class UpdateMemberCommand(

    val targetMemberId: Long,
    val partId: Long? = null,
    val role: MemberRole? = null,
    val name: String? = null,
    val nicknameEnglish: String? = null,
    val nicknameKorean: String? = null,
    val state: MemberState? = null,
    val joinDate: LocalDate? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val departmentId: Long? = null,
    val studentId: String? = null,
    val birthDate: LocalDate? = null,
    val membershipFee: Boolean? = null,
    val note: String? = null,
)
