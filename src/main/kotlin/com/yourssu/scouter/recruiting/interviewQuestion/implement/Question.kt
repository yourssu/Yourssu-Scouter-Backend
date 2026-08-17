package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException

class Question(
    val id: Long? = null,
    val partId: Long? = null,
    /** CULTURE / PART 질문은 학기별로 관리됩니다. INTRO / OUTRO 는 null 입니다. */
    val semesterId: Long? = null,
    val category: QuestionCategory,
    val content: String,
    val sortOrder: Int,
    val requirementIds: List<Long> = emptyList(),
) {

    init {
        when (category) {
            QuestionCategory.PART, QuestionCategory.CULTURE -> {
                if (requirementIds.isEmpty()) {
                    throw QuestionInvalidException("$category 질문은 최소 1개 이상의 요구조건이 필요합니다.")
                }
            }
            // INTRO / OUTRO 질문은 요구조건이 없음. 개인 인적 정보나 지원 동기 등을 묻는 기본 질문.
            QuestionCategory.INTRO, QuestionCategory.OUTRO -> Unit
        }
    }
}

