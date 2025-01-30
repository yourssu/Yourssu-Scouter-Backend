package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import java.time.LocalDate

data class ReadActiveMemberResponse(

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

    val membershipFee: Boolean,

    val note: String,
) {

    companion object {
        fun from(activeMemberDto: ActiveMemberDto): ReadActiveMemberResponse = ReadActiveMemberResponse(
            memberId = activeMemberDto.member.id,
            parts = activeMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(activeMemberDto.member.role),
            name = activeMemberDto.member.name,
            nickname = NicknameConverter.combine(
                nicknameEnglish = activeMemberDto.member.nicknameEnglish,
                nicknameKorean = activeMemberDto.member.nicknameKorean
            ),
            state = MemberStateConverter.convertToString(activeMemberDto.member.state),
            email = activeMemberDto.member.email,
            phoneNumber = activeMemberDto.member.phoneNumber,
            department = activeMemberDto.member.department.name,
            studentId = activeMemberDto.member.studentId,
            birthDate = activeMemberDto.member.birthDate,
            joinDate = activeMemberDto.member.joinDate,
            membershipFee = activeMemberDto.isMembershipFeePaid,
            note = activeMemberDto.member.note,
        )
    }
}
