package com.yourssu.scouter.hrms.business.domain.member

/**
 * 비액티브 전용 필드 일괄 갱신. [활동 기간]/[비액티브 기간] 학기 구간은 포함하지 않는다.
 * null인 항목은 기존 값을 유지한다(문자열은 공백만 오면 null로 저장).
 */
data class UpdateInactiveMemberMetadataPatch(
    val expectedReturnSemester: String? = null,
    val reason: String? = null,
    val smsReplied: Boolean? = null,
    val smsReplyDesiredPeriod: String? = null,
    val activitySemestersLabel: String? = null,
    val totalActiveSemesters: Int? = null,
    val totalInactiveSemesters: Int? = null,
) {
    fun isSpecified(): Boolean = listOf(
        expectedReturnSemester,
        reason,
        smsReplied,
        smsReplyDesiredPeriod,
        activitySemestersLabel,
        totalActiveSemesters,
        totalInactiveSemesters,
    ).any { it != null }
}
