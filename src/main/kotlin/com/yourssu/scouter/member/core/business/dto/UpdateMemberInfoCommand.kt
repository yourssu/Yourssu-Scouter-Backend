package com.yourssu.scouter.member.core.business.dto

import com.yourssu.scouter.member.support.converter.MemberRoleConverter
import com.yourssu.scouter.member.support.converter.MemberStateConverter
import com.yourssu.scouter.member.support.converter.NicknameConverter
import com.yourssu.scouter.member.core.implement.MemberRole
import com.yourssu.scouter.member.core.implement.MemberState
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
) {
    companion object {
        fun from(
            targetMemberId: Long,
            partIds: List<Long>? = null,
            role: String? = null,
            name: String? = null,
            nickname: String? = null,
            state: String? = null,
            email: String? = null,
            phoneNumber: String? = null,
            departmentId: Long? = null,
            studentId: String? = null,
            birthDate: LocalDate? = null,
            joinDate: LocalDate? = null,
            note: String? = null,
        ): UpdateMemberInfoCommand? {
            val filledFieldCount = listOf(
                partIds,
                role,
                name,
                nickname,
                state,
                email,
                phoneNumber,
                departmentId,
                studentId,
                birthDate,
                joinDate,
                note,
            ).count { it != null }

            if (filledFieldCount == 0) {
                return null
            }

            return UpdateMemberInfoCommand(
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
            )
        }
    }
}
