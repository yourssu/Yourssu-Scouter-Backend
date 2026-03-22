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
    val activePeriod: ReadSemesterPeriodInMemberResponse,
    val expectedReturnSemester: String?,
    val inactivePeriod: ReadSemesterPeriodInMemberResponse,
    val reason: String?,
    val smsReplied: Boolean?,
    val smsReplyDesiredPeriod: String?,
    @field:Schema(description = "비액티브 시트 활동학기 표시용 원문(없으면 null)")
    val activitySemestersLabel: String?,
    @field:Schema(description = "총 활동 학기 수(없으면 null)")
    val totalActiveSemesters: Int?,
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
                note = inactiveMemberDto.member.note,
            )
    }
}

data class ReadInactiveMemberResponse(

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

    val activePeriod: ReadSemesterPeriodInMemberResponse,

    val expectedReturnSemester: String?,

    val inactivePeriod: ReadSemesterPeriodInMemberResponse,

    val reason: String?,

    val smsReplied: Boolean?,

    val smsReplyDesiredPeriod: String?,

    @field:Schema(description = "비액티브 시트 활동학기 표시용 원문(없으면 null)")
    val activitySemestersLabel: String?,

    @field:Schema(description = "총 활동 학기 수(없으면 null)")
    val totalActiveSemesters: Int?,

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
            note = inactiveMemberDto.member.note,
            isSensitiveMasked = false,
        )
    }
}
