package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.InactiveActivitySemestersPatch
import com.yourssu.scouter.hrms.business.domain.member.UpdateInactiveMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateInactiveMemberMetadataPatch
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberInfoCommand
import com.yourssu.scouter.hrms.business.support.exception.MemberFieldNotEditableException
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class UpdateInactiveMemberRequest(
    @field:Schema(description = "파트 ID 목록", example = "[1,2]")
    val partIds: List<Long>? = null,
    @field:Schema(description = "멤버 역할", example = "MEMBER")
    val role: String? = null,
    @field:Schema(description = "이름", example = "홍길동")
    val name: String? = null,
    @field:Schema(description = "닉네임(영문/한글 조합)", example = "gil동")
    val nickname: String? = null,
    @field:Schema(description = "멤버 상태", example = "INACTIVE")
    val state: String? = null,
    @field:Schema(description = "이메일", example = "gildong@example.com")
    val email: String? = null,
    @field:Schema(description = "전화번호", example = "01012345678")
    val phoneNumber: String? = null,
    @field:Schema(description = "학과/소속 ID", example = "7")
    val departmentId: Long? = null,
    @field:Schema(description = "학번", example = "20201234")
    val studentId: String? = null,
    @field:Schema(example = "2003-09-23")
    val birthDate: LocalDate? = null,
    @field:Schema(example = "2024-01-01")
    val joinDate: LocalDate? = null,
    @field:Schema(description = "비고", example = "메모")
    val note: String? = null,

    @field:Schema(description = "복귀 예정 학기(조회와 동일 yy-term, 예: 25-1)", example = "25-1")
    val expectedReturnSemester: String? = null,
    @field:Schema(description = "비액티브 사유", example = "개인 사정")
    val reason: String? = null,
    @field:Schema(description = "문자 회신 여부", example = "true")
    val smsReplied: Boolean? = null,
    @field:Schema(description = "문자 회신 희망 시기(자유 텍스트)", example = "다음 학기 시작 전")
    val smsReplyDesiredPeriod: String? = null,
    @field:Schema(description = "비액티브 시트 활동학기 표시용 원문")
    val activitySemestersLabel: String? = null,
    @field:Schema(description = "총 활동 학기 수")
    val totalActiveSemesters: Int? = null,
    @field:Schema(description = "총 비액티브 학기 수")
    val totalInactiveSemesters: Int? = null,

    @field:Schema(
        description = "하위 호환용. [activitySemestersLabel]·[totalActiveSemesters]를 객체로 보낼 때 사용. " +
            "같은 필드를 본문 최상위와 동시에내면 이 객체의 값이 우선한다.",
    )
    val activitySemestersPatch: InactiveActivitySemestersPatch? = null,

    @field:Schema(description = "수정 불가. 조회 전용이며 본문에 포함하면 400 (Member-007).", hidden = true)
    val activePeriod: NonEditableSemesterPeriodBody? = null,
    @field:Schema(description = "수정 불가. 조회 전용이며 본문에 포함하면 400 (Member-007).", hidden = true)
    val inactivePeriod: NonEditableSemesterPeriodBody? = null,
    @field:Schema(description = "수정 불가. 조회 전용이며 본문에 포함하면 400 (Member-007).", hidden = true)
    val activeSemesterCountLabel: String? = null,
    @field:Schema(description = "수정 불가. 조회 전용이며 본문에 포함하면 400 (Member-007).", hidden = true)
    val inactiveSemesterCountLabel: String? = null,
) {

    fun toCommand(targetMemberId: Long): UpdateInactiveMemberCommand {
        rejectNonEditableInactiveFields()

        val mergedMetadata = mergeInactiveMetadata(
            expectedReturnSemester = expectedReturnSemester,
            reason = reason,
            smsReplied = smsReplied,
            smsReplyDesiredPeriod = smsReplyDesiredPeriod,
            activitySemestersLabel = activitySemestersLabel,
            totalActiveSemesters = totalActiveSemesters,
            totalInactiveSemesters = totalInactiveSemesters,
            activitySemestersPatch = activitySemestersPatch,
        )
        return UpdateInactiveMemberCommand(
            targetMemberId = targetMemberId,
            updateMemberInfoCommand = UpdateMemberInfoCommand.from(
                targetMemberId = targetMemberId,
                partIds = partIds,
                role = role,
                name = name,
                nickname = nickname,
                state = state,
                email = email,
                phoneNumber = phoneNumber,
                departmentId = departmentId,
                studentId = studentId,
                birthDate = birthDate,
                joinDate = joinDate,
                note = note,
            ),
            inactiveMetadataPatch = mergedMetadata,
        )
    }

    private fun rejectNonEditableInactiveFields() {
        if (activePeriod != null) {
            throw MemberFieldNotEditableException(
                "비액티브 멤버의 활동 기간(activePeriod)은 API로 직접 수정할 수 없습니다.",
            )
        }
        if (inactivePeriod != null) {
            throw MemberFieldNotEditableException(
                "비액티브 멤버의 비액티브 기간(inactivePeriod)은 API로 직접 수정할 수 없습니다.",
            )
        }
        if (activeSemesterCountLabel != null) {
            throw MemberFieldNotEditableException(
                "activeSemesterCountLabel은 조회용 표시 필드이며 수정할 수 없습니다.",
            )
        }
        if (inactiveSemesterCountLabel != null) {
            throw MemberFieldNotEditableException(
                "inactiveSemesterCountLabel은 조회용 표시 필드이며 수정할 수 없습니다.",
            )
        }
    }

    private companion object {
        fun mergeInactiveMetadata(
            expectedReturnSemester: String?,
            reason: String?,
            smsReplied: Boolean?,
            smsReplyDesiredPeriod: String?,
            activitySemestersLabel: String?,
            totalActiveSemesters: Int?,
            totalInactiveSemesters: Int?,
            activitySemestersPatch: InactiveActivitySemestersPatch?,
        ): UpdateInactiveMemberMetadataPatch? {
            val label = activitySemestersPatch?.activitySemestersLabel ?: activitySemestersLabel
            val totalActive = activitySemestersPatch?.totalActiveSemesters ?: totalActiveSemesters
            val patch = UpdateInactiveMemberMetadataPatch(
                expectedReturnSemester = expectedReturnSemester,
                reason = reason,
                smsReplied = smsReplied,
                smsReplyDesiredPeriod = smsReplyDesiredPeriod,
                activitySemestersLabel = label,
                totalActiveSemesters = totalActive,
                totalInactiveSemesters = totalInactiveSemesters,
            )
            return patch.takeIf { it.isSpecified() }
        }
    }
}
