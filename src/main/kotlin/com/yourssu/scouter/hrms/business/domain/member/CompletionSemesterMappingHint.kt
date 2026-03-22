package com.yourssu.scouter.hrms.business.domain.member

/**
 * 수료 시트 11열을 DB 학기로 못 풀었을 때, 어떤 멤버 행인지 알 수 있도록 이름·닉네임을 붙인다.
 * [rawKey]는 시트에서 읽은 문자열(trim); 빈 칸은 "" (화면에서 "(빈칸)" 처리).
 */
data class CompletionSemesterMemberLabel(
    val name: String,
    val nickname: String,
)

data class CompletionSemesterMappingHint(
    val rawKey: String,
    val memberLabels: List<CompletionSemesterMemberLabel>,
)
