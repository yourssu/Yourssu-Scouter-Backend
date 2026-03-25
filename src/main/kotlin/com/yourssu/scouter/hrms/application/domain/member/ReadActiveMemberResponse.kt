package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/** 목록 API용 아이템 (isSensitiveMasked 없음, 상단 래퍼에서 한 번만 내려감) */
data class ReadActiveMemberListItemResponse(
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
    @field:Schema(description = "멤버 상태", example = "ACTIVE")
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
    @field:Schema(description = "회비 납부 여부(민감정보 마스킹 시 null)", example = "true")
    val membershipFee: Boolean?,
    @field:Schema(description = "학년(민감정보 마스킹 시 null)", example = "3")
    val grade: Int?,
    @field:Schema(description = "재휴학 여부", example = "false")
    val isOnLeave: Boolean?,
    @field:Schema(description = "비고(민감정보 마스킹 시 null)", example = "메모")
    val note: String?,
) {
    companion object {
        fun from(activeMemberDto: ActiveMemberDto): ReadActiveMemberListItemResponse =
            ReadActiveMemberListItemResponse(
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
                grade = activeMemberDto.grade,
                isOnLeave = activeMemberDto.isOnLeave,
                note = activeMemberDto.member.note,
            )
    }
}

data class ReadActiveMemberResponse(
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
    @field:Schema(description = "멤버 상태", example = "ACTIVE")
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
    @field:Schema(description = "회비 납부 여부(민감정보 마스킹 시 null)", example = "true")
    val membershipFee: Boolean?,
    @field:Schema(description = "학년(민감정보 마스킹 시 null)", example = "3")
    val grade: Int?,
    @field:Schema(description = "재휴학 여부", example = "false")
    val isOnLeave: Boolean?,
    @field:Schema(description = "비고(민감정보 마스킹 시 null)", example = "메모")
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
            grade = activeMemberDto.grade,
            isOnLeave = activeMemberDto.isOnLeave,
            note = activeMemberDto.member.note,
            isSensitiveMasked = false,
        )
    }
}
