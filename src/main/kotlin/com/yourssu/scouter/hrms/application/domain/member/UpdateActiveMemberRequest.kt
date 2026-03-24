package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.UpdateActiveMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberInfoCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

class UpdateActiveMemberRequest(
    @field:Schema(description = "파트 ID 목록", example = "[1,2]")
    val partIds: List<Long>? = null,
    @field:Schema(description = "멤버 역할", example = "MEMBER")
    val role: String? = null,
    @field:Schema(description = "이름", example = "홍길동")
    val name: String? = null,
    @field:Schema(description = "닉네임(영문/한글 조합)", example = "gil동")
    val nickname: String? = null,
    @field:Schema(description = "멤버 상태", example = "ACTIVE")
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

    @field:Schema(description = "회비 납부 여부", example = "true")
    val membershipFee: Boolean? = null,

    @field:Schema(description = "학년 (API 전용, 엑셀 파싱 X). 미전송 시 null 유지.", example = "3")
    val grade: Int? = null,

    @field:Schema(description = "재휴학 여부 (API 전용, 엑셀 파싱 X). 미전송 시 null 유지.", example = "false")
    val isOnLeave: Boolean? = null,

    @field:Schema(description = "비고", example = "메모")
    val note: String? = null,
) {

    fun toCommand(targetMemberId: Long) = UpdateActiveMemberCommand(
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
        isMembershipFeePaid = membershipFee,
        grade = grade,
        isOnLeave = isOnLeave,
    )
}
