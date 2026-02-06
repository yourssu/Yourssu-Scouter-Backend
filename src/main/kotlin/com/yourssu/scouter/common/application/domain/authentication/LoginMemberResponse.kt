package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.hrms.application.domain.member.ReadDivisionAndPartInMemberResponse
import com.yourssu.scouter.hrms.business.domain.member.MemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

data class LoginMemberResponse(
    val memberId: Long,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate,
    val department: String,
    val studentId: String,
    val parts: List<ReadDivisionAndPartInMemberResponse>,
    val role: String,
    val nickname: String,
    val state: String,
    val joinDate: LocalDate,
    val stateUpdatedTime: Instant,
    val createdTime: LocalDateTime,
    val updatedTime: LocalDateTime,
) {

    companion object {
        fun from(memberDto: MemberDto): LoginMemberResponse = LoginMemberResponse(
            memberId = memberDto.id,
            name = memberDto.name,
            email = memberDto.email,
            phoneNumber = memberDto.phoneNumber,
            birthDate = memberDto.birthDate,
            department = memberDto.department.name,
            studentId = memberDto.studentId,
            parts = memberDto.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(memberDto.role),
            nickname = NicknameConverter.combine(
                nicknameEnglish = memberDto.nicknameEnglish,
                nicknameKorean = memberDto.nicknameKorean,
            ),
            state = MemberStateConverter.convertToString(memberDto.state),
            joinDate = memberDto.joinDate,
            stateUpdatedTime = memberDto.stateUpdatedTime,
            createdTime = memberDto.createdTime,
            updatedTime = memberDto.updatedTime,
        )
    }
}
