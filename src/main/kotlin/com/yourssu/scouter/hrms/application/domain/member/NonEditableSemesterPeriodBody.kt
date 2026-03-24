package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 조회 전용 학기 구간을 PATCH에 실었는지 감지하기 위한 타입.
 * JSON에 `activePeriod` 등 키가 있으면 인스턴스가 생기므로, null 여부만으로 수정 시도 여부를 판별한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NonEditableSemesterPeriodBody(
    val startSemester: String? = null,
    val endSemester: String? = null,
)
