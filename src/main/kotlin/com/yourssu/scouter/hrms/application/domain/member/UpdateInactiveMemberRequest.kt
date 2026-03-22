package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.InactiveActivitySemestersPatch
import com.yourssu.scouter.hrms.business.domain.member.UpdateInactiveMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberInfoCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class UpdateInactiveMemberRequest(

    val partIds: List<Long>? = null,

    val role: String? = null,

    val name: String? = null,

    val nickname: String? = null,

    val state: String? = null,

    val email: String? = null,

    val phoneNumber: String? = null,

    val departmentId: Long? = null,

    val studentId: String? = null,

    @field:Schema(example = "2003-09-23")
    val birthDate: LocalDate? = null,

    @field:Schema(example = "2024-01-01")
    val joinDate: LocalDate? = null,

    val expectedReturnSemesterId: Long? = null,

    val note: String? = null,

    @field:Schema(
        description = "활동학기 표시 필드 일괄 갱신. 설정 시 회원정보·예정복귀 학기와 동시에 보낼 수 없다.",
    )
    val activitySemestersPatch: InactiveActivitySemestersPatch? = null,
) {

    fun toCommand(targetMemberId: Long) = UpdateInactiveMemberCommand(
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
        expectedReturnSemesterId = expectedReturnSemesterId,
        activitySemestersPatch = activitySemestersPatch,
    )
}
