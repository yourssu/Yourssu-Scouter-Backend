package com.yourssu.scouter.member.core.application.dto

import com.yourssu.scouter.member.core.business.dto.MeResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "내 정보 응답")
data class MeResponse(
    @field:Schema(description = "구글 프로필 이미지 URL")
    val profileImageUrl: String,
    @field:Schema(description = "멤버 조회 응답")
    val member: MemberResponse,
) {

    companion object {
        fun from(result: MeResult): MeResponse = MeResponse(
            profileImageUrl = result.profileImageUrl,
            member = MemberResponse.from(result.member),
        )
    }
}
