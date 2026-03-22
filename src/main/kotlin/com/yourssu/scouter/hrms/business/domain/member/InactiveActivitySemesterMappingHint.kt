package com.yourssu.scouter.hrms.business.domain.member

/**
 * 비액티브 시트 활동학기(11열) 값이 DB 학기로 해석되지 않을 때, yy-s 등으로 매핑한다.
 * [rawKey]는 해당 셀의 표시 문자열 trim.
 */
data class InactiveActivitySemesterMappingHint(
    val rawKey: String,
    val memberLabels: List<CompletionSemesterMemberLabel>,
)
