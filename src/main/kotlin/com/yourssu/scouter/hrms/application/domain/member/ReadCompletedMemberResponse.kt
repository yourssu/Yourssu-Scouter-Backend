package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.hrms.business.domain.member.CompletedMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/** 목록 API용 아이템 (isSensitiveMasked 없음) */
data class ReadCompletedMemberListItemResponse(
    @field:Schema(description = "멤버 PK")
    val memberId: Long,
    @field:Schema(description = "소속 구분·파트 목록")
    val parts: List<ReadDivisionAndPartInMemberResponse>,
    @field:Schema(description = "역할(Lead 등)")
    val role: String,
    @field:Schema(description = "이름")
    val name: String,
    @field:Schema(description = "닉네임. 형식: 영어(한글발음)")
    val nickname: String,
    @field:Schema(description = "멤버 상태 한글 라벨")
    val state: String,
    @field:Schema(description = "유어슈 이메일")
    val email: String,
    @field:Schema(description = "연락처")
    val phoneNumber: String?,
    @field:Schema(description = "전공")
    val department: String,
    @field:Schema(description = "학번")
    val studentId: String?,
    @field:Schema(description = "생년월일")
    val birthDate: LocalDate?,
    @field:Schema(description = "가입일")
    val joinDate: LocalDate,
    @field:Schema(description = "수료 학기 문자열(SemesterConverter 형식, 예: 25-1)")
    val completionSemester: String?,
    @field:Schema(description = "비고")
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
                completionSemester = SemesterConverter.convertToIntString(completedMemberDto.completionSemester),
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

    val completionSemester: String?,

    val note: String?,

    @field:Schema(
        description = "민감정보(전화번호, 생년월일, 학번, 비고, 수료 학기)가 마스킹되어 null로 내려가는지 여부",
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
            completionSemester = SemesterConverter.convertToIntString(completedMemberDto.completionSemester),
            note = completedMemberDto.member.note,
            isSensitiveMasked = false,
        )
    }
}
