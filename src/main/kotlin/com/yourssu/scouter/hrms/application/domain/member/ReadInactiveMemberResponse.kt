package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.hrms.business.domain.member.InactiveMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/** 목록 API용 아이템 (isSensitiveMasked 없음) */
data class ReadInactiveMemberListItemResponse(
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
    @field:Schema(description = "멤버 상태", example = "INACTIVE")
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
    @field:Schema(description = "활동 기간(학기 범위)")
    val activePeriod: ReadSemesterPeriodInMemberResponse,
    @field:Schema(description = "복귀 예정 학기(없으면 null)", example = "2025-1")
    val expectedReturnSemester: String?,
    @field:Schema(description = "비액티브 기간(학기 범위)")
    val inactivePeriod: ReadSemesterPeriodInMemberResponse,
    @field:Schema(description = "비액티브 사유(민감정보 마스킹 시 null)", example = "개인 사정")
    val reason: String?,
    @field:Schema(description = "문자 회신 여부(민감정보 마스킹 시 null)", example = "true")
    val smsReplied: Boolean?,
    @field:Schema(description = "문자 회신 희망 시기(자유 텍스트, 없으면 null)", example = "다음 학기 시작 전")
    val smsReplyDesiredPeriod: String?,
    @field:Schema(description = "비액티브 시트 활동학기 표시용 원문(없으면 null)")
    val activitySemestersLabel: String?,
    @field:Schema(description = "총 활동 학기 수(없으면 null)")
    val totalActiveSemesters: Int?,
    @field:Schema(description = "총 비액티브 학기 수(없으면 null)")
    val totalInactiveSemesters: Int?,
    @field:Schema(description = "활동 기간 표기용 학기 수 라벨(예: 3학기, 모르면 null)")
    val activeSemesterCountLabel: String?,
    @field:Schema(description = "비액티브 기간 표기용 학기 수 라벨(예: 2학기, 모르면 null)")
    val inactiveSemesterCountLabel: String?,
    val note: String?,
) {
    companion object {
        fun from(inactiveMemberDto: InactiveMemberDto): ReadInactiveMemberListItemResponse =
            ReadInactiveMemberListItemResponse(
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
                reason = inactiveMemberDto.reason,
                smsReplied = inactiveMemberDto.smsReplied,
                smsReplyDesiredPeriod = inactiveMemberDto.smsReplyDesiredPeriod,
                activitySemestersLabel = inactiveMemberDto.activitySemestersLabel,
                totalActiveSemesters = inactiveMemberDto.totalActiveSemesters,
                totalInactiveSemesters = inactiveMemberDto.totalInactiveSemesters,
                activeSemesterCountLabel = toSemesterCountLabel(inactiveMemberDto.totalActiveSemesters),
                inactiveSemesterCountLabel = toSemesterCountLabel(inactiveMemberDto.totalInactiveSemesters),
                note = inactiveMemberDto.member.note,
            )
    }
}

data class ReadInactiveMemberResponse(
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
    @field:Schema(description = "멤버 상태", example = "INACTIVE")
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
    @field:Schema(description = "활동 기간(학기 범위)")
    val activePeriod: ReadSemesterPeriodInMemberResponse,
    @field:Schema(description = "복귀 예정 학기(없으면 null)", example = "2025-1")
    val expectedReturnSemester: String?,
    @field:Schema(description = "비액티브 기간(학기 범위)")
    val inactivePeriod: ReadSemesterPeriodInMemberResponse,
    @field:Schema(description = "비액티브 사유(민감정보 마스킹 시 null)", example = "개인 사정")
    val reason: String?,
    @field:Schema(description = "문자 회신 여부(민감정보 마스킹 시 null)", example = "true")
    val smsReplied: Boolean?,

    @field:Schema(description = "문자 회신 희망 시기(자유 텍스트, 없으면 null)", example = "다음 학기 시작 전")
    val smsReplyDesiredPeriod: String?,

    @field:Schema(description = "비액티브 시트 활동학기 표시용 원문(없으면 null)")
    val activitySemestersLabel: String?,

    @field:Schema(description = "총 활동 학기 수(없으면 null)")
    val totalActiveSemesters: Int?,

    @field:Schema(description = "총 비액티브 학기 수(없으면 null)")
    val totalInactiveSemesters: Int?,

    @field:Schema(description = "활동 기간 표기용 학기 수 라벨(예: 3학기, 모르면 null)")
    val activeSemesterCountLabel: String?,

    @field:Schema(description = "비액티브 기간 표기용 학기 수 라벨(예: 2학기, 모르면 null)")
    val inactiveSemesterCountLabel: String?,

    val note: String?,

    @field:Schema(
        description = "민감정보(전화번호, 생년월일, 학번, 비고, 복귀 예정 시기, 비액티브 사유, 활동학기 표시문구, 문자회신 관련)가 마스킹되어 null로 내려가는지 여부",
        example = "false",
    )
    val isSensitiveMasked: Boolean,
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
            reason = inactiveMemberDto.reason,
            smsReplied = inactiveMemberDto.smsReplied,
            smsReplyDesiredPeriod = inactiveMemberDto.smsReplyDesiredPeriod,
            activitySemestersLabel = inactiveMemberDto.activitySemestersLabel,
            totalActiveSemesters = inactiveMemberDto.totalActiveSemesters,
            totalInactiveSemesters = inactiveMemberDto.totalInactiveSemesters,
            activeSemesterCountLabel = toSemesterCountLabel(inactiveMemberDto.totalActiveSemesters),
            inactiveSemesterCountLabel = toSemesterCountLabel(inactiveMemberDto.totalInactiveSemesters),
            note = inactiveMemberDto.member.note,
            isSensitiveMasked = false,
        )
    }
}

private fun toSemesterCountLabel(explicitCount: Int?): String? = explicitCount?.takeIf { it > 0 }?.let { "${it}학기" }
