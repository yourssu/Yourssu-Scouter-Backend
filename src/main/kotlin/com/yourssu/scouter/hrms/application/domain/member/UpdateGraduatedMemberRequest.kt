package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.UpdateGraduatedMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberInfoCommand
import com.yourssu.scouter.hrms.business.support.exception.MemberFieldNotEditableException
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class UpdateGraduatedMemberRequest(
    @field:Schema(description = "파트 ID 목록", example = "[1,2]")
    val partIds: List<Long>? = null,
    @field:Schema(description = "멤버 역할", example = "MEMBER")
    val role: String? = null,
    @field:Schema(description = "이름", example = "홍길동")
    val name: String? = null,
    @field:Schema(description = "닉네임(영문/한글 조합)", example = "gil동")
    val nickname: String? = null,
    @field:Schema(description = "멤버 상태", example = "GRADUATED")
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

    @field:Schema(description = "지도교수 의향 여부", example = "false")
    val isAdvisorDesired: Boolean? = null,

    @field:Schema(
        description = "수정 불가. 조회 전용이며 본문에 포함하면 400 (Member-007).",
        hidden = true,
    )
    val activePeriod: NonEditableSemesterPeriodBody? = null,

    @field:Schema(description = "비고", example = "메모")
    val note: String? = null,
) {

    fun toCommand(targetMemberId: Long): UpdateGraduatedMemberCommand {
        if (activePeriod != null) {
            throw MemberFieldNotEditableException("졸업 멤버의 활동 기간(activePeriod)은 API로 수정할 수 없습니다.")
        }
        return UpdateGraduatedMemberCommand(
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
            isAdvisorDesired = isAdvisorDesired,
        )
    }
}
