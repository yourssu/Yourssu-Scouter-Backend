package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.hrms.business.domain.member.MemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberRoleConverter
import com.yourssu.scouter.hrms.business.domain.member.MemberStateConverter
import com.yourssu.scouter.hrms.business.domain.member.NicknameConverter
import java.time.LocalDate

data class ReadMemberResponse(

    val memberId: Long,

    val division: String,

    val part: String,

    val role: String,

    val name: String,

    val nickname: String,

    val state: String,

    @JsonFormat(pattern = "yyyy.MM.dd")
    val joinDate: LocalDate,

    val email: String,

    val phoneNumber: String,

    val department: String,

    val studentId: String,

    @JsonFormat(pattern = "yyyy.MM.dd")
    val birthDate: LocalDate,

    val membershipFee: Boolean,

    val note: String,
) {

    companion object {
        fun from(memberDto: MemberDto): ReadMemberResponse = ReadMemberResponse(
            memberId = memberDto.id,
            division = memberDto.part.division.name,
            part = memberDto.part.name,
            role = MemberRoleConverter.convertToString(memberDto.role),
            name = memberDto.name,
            nickname = NicknameConverter.combine(memberDto.nicknameEnglish, memberDto.nicknameKorean),
            state = MemberStateConverter.convertToString(memberDto.state),
            joinDate = memberDto.joinDate,
            email = memberDto.email,
            phoneNumber = memberDto.phoneNumber,
            department = memberDto.department.name,
            studentId = memberDto.studentId,
            birthDate = memberDto.birthDate,
            membershipFee = memberDto.isMembershipFeePaid,
            note = memberDto.note,
        )
    }
}
