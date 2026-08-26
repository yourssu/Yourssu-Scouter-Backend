package com.yourssu.scouter.recruiting.interviewQuestion.business.dto

import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory

data class QuestionDto(
    val id: Long,
    val partId: Long?,
    val category: QuestionCategory,
    val content: String,
    val sortOrder: Int,
    val requirements: List<QuestionRequirementDto> = emptyList(),
) {
    companion object {
        fun from(question: Question, requirements: List<QuestionRequirementDto> = emptyList()): QuestionDto = QuestionDto(
            id = question.id!!,
            partId = question.partId,
            category = question.category,
            content = question.content,
            sortOrder = question.sortOrder,
            requirements = requirements,
        )
    }
}
