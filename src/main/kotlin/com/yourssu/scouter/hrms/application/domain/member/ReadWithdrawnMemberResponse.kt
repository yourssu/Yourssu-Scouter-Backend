package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.hrms.business.domain.member.WithdrawnMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import java.time.LocalDate

data class ReadWithdrawnMemberResponse(

    val memberId: Long,

    val parts: List<ReadDivisionAndPartInMemberResponse>,

    val role: String,

    val name: String,

    val nickname: String,

    val state: String,

    val email: String,

    val phoneNumber: String,

    val department: String,

    val studentId: String,

    @JsonFormat(pattern = "yyyy.MM.dd")
    val birthDate: LocalDate,

    @JsonFormat(pattern = "yyyy.MM.dd")
    val joinDate: LocalDate,

    val note: String,
) {

    companion object {
        fun from(withdrawnMemberDto: WithdrawnMemberDto): ReadWithdrawnMemberResponse = ReadWithdrawnMemberResponse(
            memberId = withdrawnMemberDto.member.id,
            parts = withdrawnMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(withdrawnMemberDto.member.role),
            name = withdrawnMemberDto.member.name,
            nickname = NicknameConverter.combine(
                nicknameEnglish = withdrawnMemberDto.member.nicknameEnglish,
                nicknameKorean = withdrawnMemberDto.member.nicknameKorean
            ),
            state = MemberStateConverter.convertToString(withdrawnMemberDto.member.state),
            email = withdrawnMemberDto.member.email,
            phoneNumber = withdrawnMemberDto.member.phoneNumber,
            department = withdrawnMemberDto.member.department.name,
            studentId = withdrawnMemberDto.member.studentId,
            birthDate = withdrawnMemberDto.member.birthDate,
            joinDate = withdrawnMemberDto.member.joinDate,
            note = withdrawnMemberDto.member.note,
        )
    }
}
