package com.yourssu.scouter.hrms.business.domain.member

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 비액티브 [activitySemestersLabel]·[totalActiveSemesters]를 한 요청으로 갱신한다(하위 호환).
 * [UpdateInactiveMemberRequest] 최상위와 동명 필드가 있으면 이 객체의 값이 우선한다.
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
