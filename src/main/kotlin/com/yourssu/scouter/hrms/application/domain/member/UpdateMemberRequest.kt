package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberCommand
import java.time.LocalDate

data class UpdateMemberRequest(

    val partId: Long? = null,

    val role: String? = null,

    val name: String? = null,

    val nickname: String? = null,

    val state: String? = null,

    @field:JsonFormat(pattern = "yyyy.MM.dd")
    val joinDate: LocalDate? = null,

    val email: String? = null,

    val phoneNumber: String? = null,

    val departmentId: Long? = null,

    val studentId: String? = null,

    @field:JsonFormat(pattern = "yyyy.MM.dd")
    val birthDate: LocalDate? = null,

    val membershipFee: Boolean? = null,

    val note: String? = null,
) {

    fun toCommand(targetMemberId: Long): UpdateMemberCommand = UpdateMemberCommand(
        targetMemberId = targetMemberId,
        partId = partId,
        role = role?.let { MemberRoleConverter.convertToEnum(it) },
        name = name,
        nicknameEnglish = nickname?.let { NicknameConverter.extractNickname(it) },
        nicknameKorean = nickname?.let { NicknameConverter.extractPronunciation(it) },
        state = state?.let { MemberStateConverter.convertToEnum(it) },
        joinDate = joinDate,
        email = email,
        phoneNumber = phoneNumber,
        departmentId = departmentId,
        studentId = studentId,
        birthDate = birthDate,
        membershipFee = membershipFee,
        note = note,
    )
}
