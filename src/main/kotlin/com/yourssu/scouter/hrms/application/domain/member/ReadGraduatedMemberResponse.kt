package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.GraduatedMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/** 목록 API용 아이템 (isSensitiveMasked 없음) */
data class ReadGraduatedMemberListItemResponse(
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
    @field:Schema(description = "멤버 상태", example = "GRADUATED")
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
    @field:Schema(description = "활동 기간(학기 범위, 없으면 null)")
    val activePeriod: ReadSemesterPeriodInMemberResponse?,
    @field:Schema(description = "지도교수 의향 여부", example = "false")
    val isAdvisorDesired: Boolean,
    @field:Schema(description = "비고(민감정보 마스킹 시 null)", example = "메모")
    val note: String?,
) {
    companion object {
        fun from(graduatedMemberDto: GraduatedMemberDto): ReadGraduatedMemberListItemResponse =
            ReadGraduatedMemberListItemResponse(
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

data class ReadGraduatedMemberResponse(
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
    @field:Schema(description = "멤버 상태", example = "GRADUATED")
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
    @field:Schema(description = "활동 기간(학기 범위, 없으면 null)")
    val activePeriod: ReadSemesterPeriodInMemberResponse?,
    @field:Schema(description = "지도교수 의향 여부", example = "false")
    val isAdvisorDesired: Boolean,
    @field:Schema(description = "비고(민감정보 마스킹 시 null)", example = "메모")
    val note: String?,

    @field:Schema(
        description = "민감정보(전화번호, 생년월일, 학번, 비고, 졸업 세부 기간)가 마스킹되어 null로 내려가는지 여부",
        example = "false",
    )
    val isSensitiveMasked: Boolean,
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
            isSensitiveMasked = false,
        )
    }
}
