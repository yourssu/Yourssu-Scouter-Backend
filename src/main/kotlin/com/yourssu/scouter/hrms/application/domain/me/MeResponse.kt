package com.yourssu.scouter.hrms.application.domain.me

import com.yourssu.scouter.hrms.application.domain.member.ReadActiveMemberListItemResponse
import com.yourssu.scouter.hrms.application.domain.member.ReadCompletedMemberListItemResponse
import com.yourssu.scouter.hrms.application.domain.member.ReadGraduatedMemberListItemResponse
import com.yourssu.scouter.hrms.application.domain.member.ReadInactiveMemberListItemResponse
import com.yourssu.scouter.hrms.application.domain.member.ReadMemberResponse
import com.yourssu.scouter.hrms.application.domain.member.ReadWithdrawnMemberListItemResponse
import com.yourssu.scouter.hrms.business.domain.me.MeResult
import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.CompletedMemberDto
import com.yourssu.scouter.hrms.business.domain.member.GraduatedMemberDto
import com.yourssu.scouter.hrms.business.domain.member.InactiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.WithdrawnMemberDto
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * 내 정보 응답. [member]는 현재 상태(액티브/비액티브/수료/졸업/탈퇴)의 members 목록 조회 응답(*ListItemResponse)과
 * 동일한 타입/필드를 그대로 사용하므로, members 조회 응답이 바뀌면 함께 바뀐다.
 * note(비고)는 members 목록 조회에서도 민감정보로 마스킹되는 필드이므로 me 응답에서도 동일하게 숨긴다.
 */
@Schema(description = "내 정보 응답")
data class MeResponse(
    @field:Schema(description = "구글 프로필 이미지 URL")
    val profileImageUrl: String,
    @field:Schema(description = "상태 변경 시간")
    val stateUpdatedTime: Instant,
    @field:Schema(description = "생성 시간")
    val createdTime: Instant,
    @field:Schema(description = "수정 시간")
    val updatedTime: Instant,
    @field:Schema(
        description = "멤버 정보(상태별 members 목록 조회 응답과 동일한 필드 구성)",
        implementation = ReadMemberResponse::class,
    )
    val member: ReadMemberResponse,
) {

    companion object {
        fun from(result: MeResult): MeResponse = MeResponse(
            profileImageUrl = result.profileImageUrl,
            stateUpdatedTime = result.member.member.stateUpdatedTime,
            createdTime = result.member.member.createdTime,
            updatedTime = result.member.member.updatedTime,
            member = when (val memberState = result.member) {
                is ActiveMemberDto -> ReadActiveMemberListItemResponse.from(memberState).copy(note = null)
                is InactiveMemberDto -> ReadInactiveMemberListItemResponse.from(memberState).copy(note = null)
                is CompletedMemberDto -> ReadCompletedMemberListItemResponse.from(memberState).copy(note = null)
                is GraduatedMemberDto -> ReadGraduatedMemberListItemResponse.from(memberState).copy(note = null)
                is WithdrawnMemberDto -> ReadWithdrawnMemberListItemResponse.from(memberState).copy(note = null)
            },
        )
    }
}
