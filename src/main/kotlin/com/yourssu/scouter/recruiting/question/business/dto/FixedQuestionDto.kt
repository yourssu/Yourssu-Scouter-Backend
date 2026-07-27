package com.yourssu.scouter.recruiting.question.business.dto

import com.yourssu.scouter.recruiting.question.implement.FixedQuestion
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionCategory

data class FixedQuestionDto(
    val id: Long,
    val category: FixedQuestionCategory,
    val content: String,
    val sortOrder: Int,
) {
    companion object {
        fun from(fixedQuestion: FixedQuestion): FixedQuestionDto = FixedQuestionDto(
            id = fixedQuestion.id!!,
            category = fixedQuestion.category,
            content = fixedQuestion.content,
            sortOrder = fixedQuestion.sortOrder,
        )
    }
}
