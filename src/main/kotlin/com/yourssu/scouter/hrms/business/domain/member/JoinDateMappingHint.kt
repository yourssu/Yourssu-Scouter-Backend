package com.yourssu.scouter.hrms.business.domain.member

/**
 * 가입일 셀(raw)이 비어 있거나 파싱 실패·연도가 너무 이전이면 업로드 전에 yyyy-MM-dd로 보정한다.
 * [rawKey]는 시트 가입일 셀의 [com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe] trim 값(빈 칸은 "").
 */
data class JoinDateMappingHint(
    val sheetLabel: String,
    val rawKey: String,
    val memberLabels: List<CompletionSemesterMemberLabel>,
)
