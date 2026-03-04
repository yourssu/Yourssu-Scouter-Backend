package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.WithdrawnMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class ReadWithdrawnMemberResponse(

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

    val note: String?,

    @field:Schema(
        description = "민감정보(전화번호, 생년월일, 학번, 비고)가 마스킹되어 null로 내려가는지 여부",
        example = "false",
    )
    val isSensitiveMasked: Boolean,
) {

    companion object {
        fun from(withdrawnMemberDto: WithdrawnMemberDto): ReadWithdrawnMemberResponse = ReadWithdrawnMemberResponse(
            memberId = withdrawnMemberDto.member.id,
            parts = withdrawnMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(withdrawnMemberDto.member.role),
            name = withdrawnMemberDto.member.name,
            nickname = NicknameConverter.combine(
                nicknameEnglish = withdrawnMemberDto.member.nicknameEnglish,
                nicknameKorean = withdrawnMemberDto.member.nicknameKorean
            ),
            state = MemberStateConverter.convertToString(withdrawnMemberDto.member.state),
            email = withdrawnMemberDto.member.email,
            phoneNumber = withdrawnMemberDto.member.phoneNumber,
            department = withdrawnMemberDto.member.department.name,
            studentId = withdrawnMemberDto.member.studentId,
            birthDate = withdrawnMemberDto.member.birthDate,
            joinDate = withdrawnMemberDto.member.joinDate,
            note = withdrawnMemberDto.member.note,
            isSensitiveMasked = false,
        )
    }
}
