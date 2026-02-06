package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.hrms.application.domain.member.ReadDivisionAndPartInMemberResponse
import com.yourssu.scouter.hrms.business.domain.member.MemberDto
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "로그인 멤버 정보")
data class LoginMemberResponse(
    @field:Schema(description = "멤버 ID", example = "1")
    val memberId: Long,
    @field:Schema(description = "이름", example = "홍길동")
    val name: String,
    @field:Schema(description = "이메일", example = "hong@soongsil.ac.kr")
    val email: String,
    @field:Schema(description = "전화번호", example = "010-1234-5678")
    val phoneNumber: String,
    @field:Schema(description = "생년월일", example = "2000-01-01")
    val birthDate: LocalDate,
    @field:Schema(description = "학과", example = "컴퓨터학부")
    val department: String,
    @field:Schema(description = "학번", example = "20210001")
    val studentId: String,
    @field:Schema(description = "소속 파트 목록")
    val parts: List<ReadDivisionAndPartInMemberResponse>,
    @field:Schema(description = "역할", example = "MEMBER")
    val role: String,
    @field:Schema(description = "닉네임", example = "hong(홍)")
    val nickname: String,
    @field:Schema(description = "상태", example = "활동")
    val state: String,
    @field:Schema(description = "가입일", example = "2024-03-01")
    val joinDate: LocalDate,
    @field:Schema(description = "상태 변경 시간")
    val stateUpdatedTime: Instant,
    @field:Schema(description = "생성 시간")
    val createdTime: LocalDateTime,
    @field:Schema(description = "수정 시간")
    val updatedTime: LocalDateTime,
) {

    companion object {
        fun from(memberDto: MemberDto): LoginMemberResponse = LoginMemberResponse(
            memberId = memberDto.id,
            name = memberDto.name,
            email = memberDto.email,
            phoneNumber = memberDto.phoneNumber,
            birthDate = memberDto.birthDate,
            department = memberDto.department.name,
            studentId = memberDto.studentId,
            parts = memberDto.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(memberDto.role),
            nickname = NicknameConverter.combine(
                nicknameEnglish = memberDto.nicknameEnglish,
                nicknameKorean = memberDto.nicknameKorean,
            ),
            state = MemberStateConverter.convertToString(memberDto.state),
            joinDate = memberDto.joinDate,
            stateUpdatedTime = memberDto.stateUpdatedTime,
            createdTime = memberDto.createdTime,
            updatedTime = memberDto.updatedTime,
        )
    }
}
