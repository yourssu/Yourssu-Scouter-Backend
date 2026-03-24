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
    @field:Schema(description = "멤버 ID", example = "123")
    val memberId: Long,
    @field:Schema(description = "소속 파트 목록")
    val parts: List<ReadDivisionAndPartInMemberResponse>,
    @field:Schema(description = "멤버 역할", example = "MEMBER")
    val role: String,
    @field:Schema(description = "이름", example = "홍길동")
    val name: String,
    @field:Schema(description = "닉네임(영문/한글 조합)", example = "gil동")
    val nickname: String,
    @field:Schema(description = "멤버 상태", example = "COMPLETED")
    val state: String,
    @field:Schema(description = "이메일", example = "gildong@example.com")
    val email: String,
    @field:Schema(description = "전화번호(민감정보 마스킹 시 null)", example = "01012345678")
    val phoneNumber: String?,
    @field:Schema(description = "학과(또는 소속)", example = "컴퓨터학부")
    val department: String,
    @field:Schema(description = "학번(민감정보 마스킹 시 null)", example = "20201234")
    val studentId: String?,
    @field:Schema(description = "생년월일(민감정보 마스킹 시 null)", example = "2003-09-23")
    val birthDate: LocalDate?,
    @field:Schema(description = "가입일", example = "2024-01-01")
    val joinDate: LocalDate,
    @field:Schema(description = "수료 학기(없으면 null, yy-term)", example = "25-1")
    val completionSemester: String?,
    @field:Schema(description = "비고(민감정보 마스킹 시 null)", example = "메모")
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
    @field:Schema(description = "멤버 ID", example = "123")
    val memberId: Long,
    @field:Schema(description = "소속 파트 목록")
    val parts: List<ReadDivisionAndPartInMemberResponse>,
    @field:Schema(description = "멤버 역할", example = "MEMBER")
    val role: String,
    @field:Schema(description = "이름", example = "홍길동")
    val name: String,
    @field:Schema(description = "닉네임(영문/한글 조합)", example = "gil동")
    val nickname: String,
    @field:Schema(description = "멤버 상태", example = "COMPLETED")
    val state: String,
    @field:Schema(description = "이메일", example = "gildong@example.com")
    val email: String,
    @field:Schema(description = "전화번호(민감정보 마스킹 시 null)", example = "01012345678")
    val phoneNumber: String?,
    @field:Schema(description = "학과(또는 소속)", example = "컴퓨터학부")
    val department: String,
    @field:Schema(description = "학번(민감정보 마스킹 시 null)", example = "20201234")
    val studentId: String?,
    @field:Schema(description = "생년월일(민감정보 마스킹 시 null)", example = "2003-09-23")
    val birthDate: LocalDate?,
    @field:Schema(description = "가입일", example = "2024-01-01")
    val joinDate: LocalDate,
    @field:Schema(description = "수료 학기(없으면 null, yy-term)", example = "25-1")
    val completionSemester: String?,
    @field:Schema(description = "비고(민감정보 마스킹 시 null)", example = "메모")
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
