package com.yourssu.scouter.hrms.application.domain.member

import io.swagger.v3.oas.annotations.media.Schema

data class MemberSyncResponse(
    @field:Schema(description = "동기화 실패 메시지 목록", example = "[\"email 중복: test@example.com\"]")
    val failureMessages: List<String>,
    @field:Schema(description = "생성된 멤버 수", example = "12")
    val createdCount: Int,
)
