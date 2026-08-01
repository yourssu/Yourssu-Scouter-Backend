package com.yourssu.scouter.recruiting.interview.application.dto

import jakarta.validation.constraints.NotNull

data class UpdateInterviewRequirementItemRequest(
    val id: Long?,
    val content: String,
)

data class UpdateInterviewRequirementRequest(
    @field:NotNull
    val culture: List<UpdateInterviewRequirementItemRequest>,

    @field:NotNull
    val team: List<UpdateInterviewRequirementItemRequest>,

    @field:NotNull
    val job: List<UpdateInterviewRequirementItemRequest>,

    @field:NotNull
    val other: List<UpdateInterviewRequirementItemRequest>,
)
