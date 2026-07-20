package com.yourssu.scouter.hrms.application.domain.member

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 상태별 멤버 목록 조회 응답(ReadActiveMemberListItemResponse 등)을 공통으로 다루기 위한 마커 인터페이스.
 * 실제 응답은 멤버 상태(액티브/비액티브/수료/졸업/탈퇴)에 따라 아래 타입 중 하나로 내려간다.
 */
@Schema(
    description = "멤버 상태에 따라 아래 타입 중 하나로 내려가는 멤버 정보",
    oneOf = [
        ReadActiveMemberListItemResponse::class,
        ReadInactiveMemberListItemResponse::class,
        ReadCompletedMemberListItemResponse::class,
        ReadGraduatedMemberListItemResponse::class,
        ReadWithdrawnMemberListItemResponse::class,
    ],
)
sealed interface ReadMemberResponse
