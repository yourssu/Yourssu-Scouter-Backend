package com.yourssu.scouter.hrms.application.domain.me

import com.yourssu.scouter.hrms.application.domain.member.ReadDivisionAndPartInMemberResponse
import com.yourssu.scouter.hrms.business.domain.me.MeResult
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "내 정보 응답")
data class MeResponse(
    @field:Schema(description = "구글 프로필 이미지 URL")
    val profileImageUrl: String,
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
    @field:Schema(description = "닉네임", example = "piki(피키)")
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
        fun from(result: MeResult): MeResponse = MeResponse(
            profileImageUrl = result.profileImageUrl,
            memberId = result.member.id,
            name = result.member.name,
            email = result.member.email,
            phoneNumber = result.member.phoneNumber,
            birthDate = result.member.birthDate,
            department = result.member.department.name,
            studentId = result.member.studentId,
            parts = result.member.parts.map { ReadDivisionAndPartInMemberResponse.from(it) },
            role = MemberRoleConverter.convertToString(result.member.role),
            nickname = NicknameConverter.combine(
                nicknameEnglish = result.member.nicknameEnglish,
                nicknameKorean = result.member.nicknameKorean,
            ),
            state = MemberStateConverter.convertToString(result.member.state),
            joinDate = result.member.joinDate,
            stateUpdatedTime = result.member.stateUpdatedTime,
            createdTime = result.member.createdTime,
            updatedTime = result.member.updatedTime,
        )
    }
}
