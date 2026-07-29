package com.yourssu.scouter.recruiting.question.application.dto

import com.yourssu.scouter.recruiting.question.business.dto.FixedQuestionDto
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionCategory

data class ReadFixedQuestionResponse(
    val id: Long,
    val category: FixedQuestionCategory,
    val content: String,
    val sortOrder: Int,
) {
    companion object {
        fun from(dto: FixedQuestionDto): ReadFixedQuestionResponse = ReadFixedQuestionResponse(
            id = dto.id,
            category = dto.category,
            content = dto.content,
            sortOrder = dto.sortOrder,
        )
    }
}
