package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.QuestionRequirementDto

data class ReadQuestionRequirementResponse(
    val id: Long,
    val content: String,
) {
    companion object {
        fun from(dto: QuestionRequirementDto): ReadQuestionRequirementResponse = ReadQuestionRequirementResponse(
            id = dto.id,
            content = dto.content,
        )
    }
}
