package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateQuestionsRequest(
    @field:Valid val global: List<Item> = emptyList(),
    @field:Valid val culture: List<Item> = emptyList(),
) {
    data class Item(
        @field:NotBlank val content: String,
        @field:NotNull val sortOrder: Int,
        val requirementIds: List<Long> = emptyList(),
    )
}

data class PartQuestionRequest(
    val id: Long? = null,
    @field:NotBlank val content: String,
    @field:NotNull val sortOrder: Int,
    @field:NotNull val requirementIds: List<Long>,
)

data class UpdatePartQuestionsRequest(
    @field:Valid val questions: List<PartQuestionRequest>,
)
