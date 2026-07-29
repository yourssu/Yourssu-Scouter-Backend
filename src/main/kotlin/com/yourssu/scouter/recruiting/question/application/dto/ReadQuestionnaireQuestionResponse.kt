package com.yourssu.scouter.recruiting.question.application.dto

import com.yourssu.scouter.recruiting.question.business.dto.QuestionnaireQuestionDto
import com.yourssu.scouter.recruiting.question.implement.QuestionGroup

data class ReadQuestionnaireQuestionResponse(
    val id: Long,
    val group: QuestionGroup,
    val sourceQuestionId: Long?,
    val content: String,
) {
    companion object {
        fun from(dto: QuestionnaireQuestionDto): ReadQuestionnaireQuestionResponse = ReadQuestionnaireQuestionResponse(
            id = dto.id,
            group = dto.group,
            sourceQuestionId = dto.sourceQuestionId,
            content = dto.content,
        )
    }
}
