package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.hrms.business.domain.member.InactiveMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import java.time.LocalDate

data class ReadInactiveMemberResponse(

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

    val activePeriod: ReadSemesterPeriodInMemberResponse,

    val expectedReturnSemester: String,

    val inactivePeriod: ReadSemesterPeriodInMemberResponse,

    val note: String,
) {

    companion object {
        fun from(inactiveMemberDto: InactiveMemberDto): ReadInactiveMemberResponse = ReadInactiveMemberResponse(
            memberId = inactiveMemberDto.member.id,
            parts = inactiveMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(inactiveMemberDto.member.role),
            name = inactiveMemberDto.member.name,
            nickname = NicknameConverter.combine(
                nicknameEnglish = inactiveMemberDto.member.nicknameEnglish,
                nicknameKorean = inactiveMemberDto.member.nicknameKorean
            ),
            state = MemberStateConverter.convertToString(inactiveMemberDto.member.state),
            email = inactiveMemberDto.member.email,
            phoneNumber = inactiveMemberDto.member.phoneNumber,
            department = inactiveMemberDto.member.department.name,
            studentId = inactiveMemberDto.member.studentId,
            birthDate = inactiveMemberDto.member.birthDate,
            joinDate = inactiveMemberDto.member.joinDate,
            activePeriod = ReadSemesterPeriodInMemberResponse.from(inactiveMemberDto.activePeriod),
            expectedReturnSemester = SemesterConverter.convertToIntString(inactiveMemberDto.expectedReturnSemester),
            inactivePeriod = ReadSemesterPeriodInMemberResponse.from(inactiveMemberDto.inactivePeriod),
            note = inactiveMemberDto.member.note,
        )
    }
}
