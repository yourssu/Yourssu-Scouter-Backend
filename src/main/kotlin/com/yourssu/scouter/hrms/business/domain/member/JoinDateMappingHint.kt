package com.yourssu.scouter.hrms.business.domain.member

/**
 * 가입일·탈퇴일 셀(raw)이 비어 있거나 파싱 실패·연도가 너무 이전이면 업로드 전에 yyyy-MM-dd로 보정한다.
 * [sheetLabel]이 "탈퇴"이면 탈퇴 시트 4열(탈퇴일자) 표시값이다.
 * [rawKey]는 해당 날짜 셀의 [com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe] trim 값(빈 칸은 "").
 */
data class JoinDateMappingHint(
    val sheetLabel: String,
    val rawKey: String,
    val memberLabels: List<CompletionSemesterMemberLabel>,
)
