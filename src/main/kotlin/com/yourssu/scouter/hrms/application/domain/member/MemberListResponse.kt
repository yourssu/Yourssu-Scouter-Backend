package com.yourssu.scouter.hrms.application.domain.member

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 멤버 목록 API 공통 응답 래퍼.
 * [isSensitiveMasked]가 true이면 목록 내 모든 멤버의 민감 필드(전화번호, 생년월일, 학번 등)가 null로 내려갑니다.
 */
@Schema(
    description = "멤버 목록과 민감정보 마스킹 여부. isSensitiveMasked가 true면 members 내 민감 필드는 null.",
    example = """{"members":[],"isSensitiveMasked":false}""",
)
data class MemberListResponse<T>(
    @field:Schema(description = "멤버 목록")
    val members: List<T>,
    @field:Schema(description = "민감정보 마스킹 적용 여부", example = "false")
    val isSensitiveMasked: Boolean,
)
