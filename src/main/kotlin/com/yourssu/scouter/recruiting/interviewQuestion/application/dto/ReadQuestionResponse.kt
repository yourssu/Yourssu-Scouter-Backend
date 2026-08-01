package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.QuestionDto
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory

data class ReadQuestionResponse(
    val id: Long,
    val partId: Long?,
    val category: QuestionCategory,
    val content: String,
    val sortOrder: Int,
) {
    companion object {
        fun from(dto: QuestionDto): ReadQuestionResponse = ReadQuestionResponse(
            id = dto.id,
            partId = dto.partId,
            category = dto.category,
            content = dto.content,
            sortOrder = dto.sortOrder,
        )
    }
}
