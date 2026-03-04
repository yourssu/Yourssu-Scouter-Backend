package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class ReadActiveMemberResponse(

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

    val membershipFee: Boolean?,

    val note: String?,

    @field:Schema(
        description = "민감정보(전화번호, 생년월일, 학번, 회비 납부, 비고)가 마스킹되어 null로 내려가는지 여부",
        example = "false",
    )
    val isSensitiveMasked: Boolean,
) {

    companion object {
        fun from(activeMemberDto: ActiveMemberDto): ReadActiveMemberResponse = ReadActiveMemberResponse(
            memberId = activeMemberDto.member.id,
            parts = activeMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(activeMemberDto.member.role),
            name = activeMemberDto.member.name,
            nickname = NicknameConverter.combine(
                nicknameEnglish = activeMemberDto.member.nicknameEnglish,
                nicknameKorean = activeMemberDto.member.nicknameKorean
            ),
            state = MemberStateConverter.convertToString(activeMemberDto.member.state),
            email = activeMemberDto.member.email,
            phoneNumber = activeMemberDto.member.phoneNumber,
            department = activeMemberDto.member.department.name,
            studentId = activeMemberDto.member.studentId,
            birthDate = activeMemberDto.member.birthDate,
            joinDate = activeMemberDto.member.joinDate,
            membershipFee = activeMemberDto.isMembershipFeePaid,
            note = activeMemberDto.member.note,
            isSensitiveMasked = false,
        )
    }
}
