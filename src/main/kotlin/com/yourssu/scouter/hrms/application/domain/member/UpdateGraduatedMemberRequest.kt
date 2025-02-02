package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.hrms.business.domain.member.UpdateGraduatedMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberInfoCommand
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import java.time.LocalDate

data class UpdateGraduatedMemberRequest(

    val partIds: List<Long>? = null,

    val role: String? = null,

    val name: String? = null,

    val nickname: String? = null,

    val state: String? = null,

    val email: String? = null,

    val phoneNumber: String? = null,

    val departmentId: Long? = null,

    val studentId: String? = null,

    @field:JsonFormat(pattern = "yyyy.MM.dd")
    val birthDate: LocalDate? = null,

    @field:JsonFormat(pattern = "yyyy.MM.dd")
    val joinDate: LocalDate? = null,

    val isAdvisorDesired: Boolean? = null,

    val note: String? = null,
) {

    fun toCommand(targetMemberId: Long) = UpdateGraduatedMemberCommand(
        targetMemberId = targetMemberId,
        updateMemberInfoCommand = UpdateMemberInfoCommand(
            targetMemberId = targetMemberId,
            partIds = partIds,
            role = role?.let { MemberRoleConverter.convertToEnum(it) },
            name = name,
            nicknameEnglish = nickname?.let { NicknameConverter.extractNickname(it) },
            nicknameKorean = nickname?.let { NicknameConverter.extractPronunciation(it) },
            state = state?.let { MemberStateConverter.convertToEnum(it) },
            email = email,
            phoneNumber = phoneNumber,
            departmentId = departmentId,
            studentId = studentId,
            birthDate = birthDate,
            joinDate = joinDate,
            note = note,
        ),
        isAdvisorDesired = isAdvisorDesired,
    )
}
