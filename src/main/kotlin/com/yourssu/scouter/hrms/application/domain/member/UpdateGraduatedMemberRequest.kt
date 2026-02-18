package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.UpdateGraduatedMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberInfoCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class UpdateGraduatedMemberRequest(

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

    val isAdvisorDesired: Boolean? = null,

    val note: String? = null,
) {

    fun toCommand(targetMemberId: Long) = UpdateGraduatedMemberCommand(
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
