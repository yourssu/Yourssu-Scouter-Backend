package com.yourssu.scouter.recruiting.interviewQuestion.implement

/**
 * 아직 저장되지 않은, 요청으로 들어온 한 문항.
 * [AssignedQuestion]은 카테고리별 제약을 생성 시점에 강제하므로(예: PART는 sourceQuestionId 필수)
 * 카탈로그 질문 발급 전 단계의 요청 값을 그대로 담을 수 없어 별도 타입으로 받는다.
 */
data class AssignedQuestionDraft(
    val assignedMemberId: Long,
    val sourceQuestionId: Long?,
    val content: String?,
    val category: AssignedQuestionCategory,
    val isSelected: Boolean?,
    val requirementIds: List<Long>?,
) {
    /** 카탈로그에 아직 없는, 이번 요청으로 새로 만들어야 하는 PART 질문. */
    val isNewPartQuestion: Boolean
        get() = category == AssignedQuestionCategory.PART && sourceQuestionId == null
}

internal fun List<AssignedQuestion>.partSourceQuestionIds(): Set<Long> =
    filter { it.category == AssignedQuestionCategory.PART }.mapNotNull { it.sourceQuestionId }.toSet()
