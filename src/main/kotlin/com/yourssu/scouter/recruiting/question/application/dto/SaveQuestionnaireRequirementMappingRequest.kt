package com.yourssu.scouter.recruiting.question.application.dto

import com.yourssu.scouter.recruiting.question.business.dto.RequirementQuestionMappingItemCommand
import com.yourssu.scouter.recruiting.question.business.dto.SaveQuestionnaireRequirementMappingCommand
import jakarta.validation.constraints.NotNull

data class SaveQuestionnaireRequirementMappingRequest(
    val culture: List<RequirementQuestionMappingItemRequest> = emptyList(),
    val team: List<RequirementQuestionMappingItemRequest> = emptyList(),
    val job: List<RequirementQuestionMappingItemRequest> = emptyList(),
    val other: List<RequirementQuestionMappingItemRequest> = emptyList(),
) {
    fun toCommand(): SaveQuestionnaireRequirementMappingCommand = SaveQuestionnaireRequirementMappingCommand(
        culture = culture.map { it.toCommand() },
        team = team.map { it.toCommand() },
        job = job.map { it.toCommand() },
        other = other.map { it.toCommand() },
    )
}

data class RequirementQuestionMappingItemRequest(
    @field:NotNull
    val requirementId: Long,
    val questionIds: List<Long> = emptyList(),
) {
    fun toCommand(): RequirementQuestionMappingItemCommand = RequirementQuestionMappingItemCommand(
        requirementId = requirementId,
        questionIds = questionIds,
    )
}

