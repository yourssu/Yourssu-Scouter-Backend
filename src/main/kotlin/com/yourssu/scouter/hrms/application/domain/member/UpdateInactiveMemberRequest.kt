package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.hrms.business.domain.member.UpdateInactiveMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberInfoCommand
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

    @field:JsonFormat(pattern = "yyyy.MM.dd")
    val birthDate: LocalDate? = null,

    @field:JsonFormat(pattern = "yyyy.MM.dd")
    val joinDate: LocalDate? = null,

    val expectedReturnSemesterId: Long? = null,

    val note: String? = null,
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
    )
}
