package com.yourssu.scouter.member.core.application.dto

import com.yourssu.scouter.member.core.business.dto.MemberDto
import com.yourssu.scouter.member.support.converter.MemberRoleConverter
import com.yourssu.scouter.member.support.converter.MemberStateConverter
import com.yourssu.scouter.member.support.converter.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate

@Schema(description = "멤버 조회 응답")
data class MemberResponse(
    @field:Schema(description = "멤버 ID", example = "1")
    val memberId: Long,
    @field:Schema(description = "로그인 user id", example = "12", types = ["Long", "null"])
    val userId: Long?,
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
    @field:Schema(description = "닉네임", example = "piki(피키)")
    val nickname: String,
    @field:Schema(description = "상태", example = "활동")
    val state: String,
    @field:Schema(description = "가입일", example = "2024-03-01")
    val joinDate: LocalDate,
    @field:Schema(description = "상태 변경 시간")
    val stateUpdatedTime: Instant,
    @field:Schema(description = "생성 시간")
    val createdTime: Instant,
    @field:Schema(description = "수정 시간")
    val updatedTime: Instant,
) {

    companion object {
        fun from(member: MemberDto): MemberResponse = MemberResponse(
            memberId = member.id,
            userId = member.userId,
            name = member.name,
            email = member.email,
            phoneNumber = member.phoneNumber,
            birthDate = member.birthDate,
            department = member.department.name,
            studentId = member.studentId,
            parts = member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(member.role),
            nickname = NicknameConverter.combine(
                nicknameEnglish = member.nicknameEnglish,
                nicknameKorean = member.nicknameKorean,
            ),
            state = MemberStateConverter.convertToString(member.state),
            joinDate = member.joinDate,
            stateUpdatedTime = member.stateUpdatedTime,
            createdTime = member.createdTime,
            updatedTime = member.updatedTime,
        )
    }
}
