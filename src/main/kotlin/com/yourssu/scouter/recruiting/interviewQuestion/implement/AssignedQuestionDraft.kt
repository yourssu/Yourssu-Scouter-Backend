package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException

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

    init {
        // INTRO/OUTRO/CULTURE의 content와 요구조건은 인스턴스가 아닌 원본 질문(Question)에 매핑되고,
        // 그 원본은 이 요청으로 바꿀 수 없다. PART만 같은 요청으로 카탈로그까지 수정한다.
        if (category.isCatalog && category != AssignedQuestionCategory.PART) {
            if (requirementIds != null) {
                throw QuestionInvalidException("$category 질문은 요구조건을 변경할 수 없습니다.")
            }
            if (content != null) {
                throw QuestionInvalidException("$category 질문은 content를 변경할 수 없습니다.")
            }
        }
    }

    /** 카탈로그에 아직 없는, 이번 요청으로 새로 만들어야 하는 PART 질문. */
    val isNewPartQuestion: Boolean
        get() = category == AssignedQuestionCategory.PART && sourceQuestionId == null
}

internal fun List<AssignedQuestion>.partSourceQuestionIds(): Set<Long> =
    filter { it.category == AssignedQuestionCategory.PART }.mapNotNull { it.sourceQuestionId }.toSet()
