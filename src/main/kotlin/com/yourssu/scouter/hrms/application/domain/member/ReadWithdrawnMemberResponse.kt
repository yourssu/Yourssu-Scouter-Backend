package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.WithdrawnMemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/** 목록 API용 아이템 (isSensitiveMasked 없음). 탈퇴 화면용으로 연락처·학적 등은 내려가지 않음. */
data class ReadWithdrawnMemberListItemResponse(
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
    @field:Schema(description = "멤버 상태", example = "WITHDRAWN")
    val state: String,
    @field:Schema(description = "탈퇴 일자(민감정보 마스킹 시 null)", example = "2025-09-01")
    val withdrawnDate: LocalDate?,
    @field:Schema(description = "비고(민감정보 마스킹 시 null)", example = "개인 사정")
    val note: String?,
) {
    companion object {
        fun from(withdrawnMemberDto: WithdrawnMemberDto): ReadWithdrawnMemberListItemResponse =
            ReadWithdrawnMemberListItemResponse(
                memberId = withdrawnMemberDto.member.id,
                parts = withdrawnMemberDto.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
                role = MemberRoleConverter.convertToString(withdrawnMemberDto.member.role),
                name = withdrawnMemberDto.member.name,
                nickname = NicknameConverter.combine(
                    nicknameEnglish = withdrawnMemberDto.member.nicknameEnglish,
                    nicknameKorean = withdrawnMemberDto.member.nicknameKorean
                ),
                state = MemberStateConverter.convertToString(withdrawnMemberDto.member.state),
                withdrawnDate = withdrawnMemberDto.withdrawnDate,
                note = withdrawnMemberDto.member.note,
            )
    }
}

data class ReadWithdrawnMemberResponse(
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
    @field:Schema(description = "멤버 상태", example = "WITHDRAWN")
    val state: String,
    @field:Schema(description = "탈퇴 일자(민감정보 마스킹 시 null)", example = "2025-09-01")
    val withdrawnDate: LocalDate?,
    @field:Schema(description = "비고(민감정보 마스킹 시 null)", example = "개인 사정")
    val note: String?,

    @field:Schema(
        description = "민감정보(탈퇴 일자, 비고)가 마스킹되어 null로 내려가는지 여부",
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
            withdrawnDate = withdrawnMemberDto.withdrawnDate,
            note = withdrawnMemberDto.member.note,
            isSensitiveMasked = false,
        )
    }
}
