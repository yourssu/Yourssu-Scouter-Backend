package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.CompletedMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/** 목록 API용 아이템 (isSensitiveMasked 없음) */
data class ReadCompletedMemberListItemResponse(
    val memberId: Long,
    val parts: List<ReadDivisionAndPartInMemberResponse>,
    val role: String,
    val name: String,
    val nickname: String,
    val state: String,
    val email: String,
    val phoneNumber: String?,
    val department: String,
    val studentId: String?,
    val birthDate: LocalDate?,
    val joinDate: LocalDate,
    val activePeriod: ReadSemesterPeriodInMemberResponse?,
    val isAdvisorDesired: Boolean,
    val note: String?,
) {
    companion object {
        fun from(completedMemberDto: CompletedMemberDto): ReadCompletedMemberListItemResponse =
            ReadCompletedMemberListItemResponse(
                memberId = completedMemberDto.member.id,
                parts = completedMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
                role = MemberRoleConverter.convertToString(completedMemberDto.member.role),
                name = completedMemberDto.member.name,
                nickname = NicknameConverter.combine(
                    nicknameEnglish = completedMemberDto.member.nicknameEnglish,
                    nicknameKorean = completedMemberDto.member.nicknameKorean
                ),
                state = MemberStateConverter.convertToString(completedMemberDto.member.state),
                email = completedMemberDto.member.email,
                phoneNumber = completedMemberDto.member.phoneNumber,
                department = completedMemberDto.member.department.name,
                studentId = completedMemberDto.member.studentId,
                birthDate = completedMemberDto.member.birthDate,
                joinDate = completedMemberDto.member.joinDate,
                activePeriod = ReadSemesterPeriodInMemberResponse.from(completedMemberDto.activePeriod),
                isAdvisorDesired = completedMemberDto.isAdvisorDesired,
                note = completedMemberDto.member.note,
            )
    }
}

data class ReadCompletedMemberResponse(

    val memberId: Long,

    val parts: List<ReadDivisionAndPartInMemberResponse>,

    val role: String,

    val name: String,

    val nickname: String,

    val state: String,

    val email: String,

    val phoneNumber: String?,

    val department: String,

    val studentId: String?,

    val birthDate: LocalDate?,

    val joinDate: LocalDate,

    val activePeriod: ReadSemesterPeriodInMemberResponse?,

    val isAdvisorDesired: Boolean,

    val note: String?,

    @field:Schema(
        description = "민감정보(전화번호, 생년월일, 학번, 비고, 수료 세부 기간)가 마스킹되어 null로 내려가는지 여부",
        example = "false",
    )
    val isSensitiveMasked: Boolean,
) {

    companion object {
        fun from(completedMemberDto: CompletedMemberDto): ReadCompletedMemberResponse = ReadCompletedMemberResponse(
            memberId = completedMemberDto.member.id,
            parts = completedMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(completedMemberDto.member.role),
            name = completedMemberDto.member.name,
            nickname = NicknameConverter.combine(
                nicknameEnglish = completedMemberDto.member.nicknameEnglish,
                nicknameKorean = completedMemberDto.member.nicknameKorean
            ),
            state = MemberStateConverter.convertToString(completedMemberDto.member.state),
            email = completedMemberDto.member.email,
            phoneNumber = completedMemberDto.member.phoneNumber,
            department = completedMemberDto.member.department.name,
            studentId = completedMemberDto.member.studentId,
            birthDate = completedMemberDto.member.birthDate,
            joinDate = completedMemberDto.member.joinDate,
            activePeriod = ReadSemesterPeriodInMemberResponse.from(completedMemberDto.activePeriod),
            isAdvisorDesired = completedMemberDto.isAdvisorDesired,
            note = completedMemberDto.member.note,
            isSensitiveMasked = false,
        )
    }
}
