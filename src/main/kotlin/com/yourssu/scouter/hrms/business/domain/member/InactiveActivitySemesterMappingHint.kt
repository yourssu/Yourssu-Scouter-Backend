package com.yourssu.scouter.hrms.business.domain.member

/**
 * 비액티브 시트 활동학기(11열) raw를 기준으로 활동/비액티브 학기 수 매핑 입력이 필요할 때 사용한다.
 * [rawKey]는 해당 셀의 표시 문자열 trim.
 */
data class InactiveActivitySemesterMappingHint(
    val rawKey: String,
    val memberLabels: List<CompletionSemesterMemberLabel>,
)
