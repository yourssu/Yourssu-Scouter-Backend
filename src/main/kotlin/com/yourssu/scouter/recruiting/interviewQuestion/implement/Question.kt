package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException

class Question(
    val id: Long? = null,
    val partId: Long? = null,
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
            QuestionCategory.GLOBAL -> Unit
        }
    }
}
