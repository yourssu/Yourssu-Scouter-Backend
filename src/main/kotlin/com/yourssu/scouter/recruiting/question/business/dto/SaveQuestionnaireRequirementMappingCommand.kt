package com.yourssu.scouter.recruiting.question.business.dto

data class SaveQuestionnaireRequirementMappingCommand(
    val culture: List<RequirementQuestionMappingItemCommand>,
    val team: List<RequirementQuestionMappingItemCommand>,
    val job: List<RequirementQuestionMappingItemCommand>,
    val other: List<RequirementQuestionMappingItemCommand>,
) {
    fun toAllItems(): List<RequirementQuestionMappingItemCommand> {
        return culture + team + job + other
    }
}

data class RequirementQuestionMappingItemCommand(
    val requirementId: Long,
    val questionIds: List<Long>,
)
