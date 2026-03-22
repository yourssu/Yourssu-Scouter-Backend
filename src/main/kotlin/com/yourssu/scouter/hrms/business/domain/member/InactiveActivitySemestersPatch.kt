package com.yourssu.scouter.hrms.business.domain.member

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 비액티브 [activitySemestersLabel]·[totalActiveSemesters]를 한 요청으로 갱신한다.
 * 본문에 이 객체가 포함되면 두 필드는 여기서 온 값으로 **덮어쓴다**(각 프로퍼티 null이면 DB에 null 저장).
 */
@Schema(
    name = "InactiveActivitySemestersPatch",
    description = "비액티브 활동학기 표시용 원문·총 학기 수 일괄 갱신",
)
data class InactiveActivitySemestersPatch(
    @field:Schema(description = "시트 표기용 자유 텍스트", example = "23년도 1학기, 24년도 2학기~25년도 1학기")
    val activitySemestersLabel: String? = null,
    @field:Schema(description = "총 활동 학기 수", example = "3")
    val totalActiveSemesters: Int? = null,
)
