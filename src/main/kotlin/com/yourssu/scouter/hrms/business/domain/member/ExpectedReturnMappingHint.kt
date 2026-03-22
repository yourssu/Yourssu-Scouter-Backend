package com.yourssu.scouter.hrms.business.domain.member

/**
 * 비액티브 시트 예정복귀 칸 값이 DB 학기로 해석되지 않을 때, reason 메모 전에 yy-s 등으로 매핑한다.
 * [rawKey]는 해당 셀의 표시 문자열 trim.
 */
data class ExpectedReturnMappingHint(
    val rawKey: String,
    val memberLabels: List<CompletionSemesterMemberLabel>,
)
