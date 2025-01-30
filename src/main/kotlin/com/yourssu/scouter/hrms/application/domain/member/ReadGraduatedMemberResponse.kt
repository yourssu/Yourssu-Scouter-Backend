package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.hrms.business.domain.member.GraduatedMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import java.time.LocalDate

data class ReadGraduatedMemberResponse(

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

    val isAdvisorDesired: Boolean,

    val note: String,
) {

    companion object {
        fun from(graduatedMemberDto: GraduatedMemberDto): ReadGraduatedMemberResponse = ReadGraduatedMemberResponse(
            memberId = graduatedMemberDto.member.id,
            parts = graduatedMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(graduatedMemberDto.member.role),
            name = graduatedMemberDto.member.name,
            nickname = NicknameConverter.combine(
                nicknameEnglish = graduatedMemberDto.member.nicknameEnglish,
                nicknameKorean = graduatedMemberDto.member.nicknameKorean
            ),
            state = MemberStateConverter.convertToString(graduatedMemberDto.member.state),
            email = graduatedMemberDto.member.email,
            phoneNumber = graduatedMemberDto.member.phoneNumber,
            department = graduatedMemberDto.member.department.name,
            studentId = graduatedMemberDto.member.studentId,
            birthDate = graduatedMemberDto.member.birthDate,
            joinDate = graduatedMemberDto.member.joinDate,
            activePeriod = ReadSemesterPeriodInMemberResponse.from(graduatedMemberDto.activePeriod),
            isAdvisorDesired = graduatedMemberDto.isAdvisorDesired,
            note = graduatedMemberDto.member.note,
        )
    }
}
