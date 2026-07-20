package com.yourssu.scouter.hrms.business.domain.member

/** 상태별 멤버 조회 결과(ActiveMemberDto 등)를 공통으로 다루기 위한 마커 인터페이스 */
sealed interface MemberStateDto {
    val member: MemberDto
}
