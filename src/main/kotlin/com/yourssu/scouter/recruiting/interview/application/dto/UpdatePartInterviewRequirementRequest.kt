package com.yourssu.scouter.recruiting.interview.application.dto

import jakarta.validation.constraints.NotNull

data class UpdatePartInterviewRequirementItemRequest(
    val id: Long?,
    val content: String,
)

data class UpdatePartInterviewRequirementRequest(
    @field:NotNull
    val culture: List<UpdatePartInterviewRequirementItemRequest>,

    @field:NotNull
    val team: List<UpdatePartInterviewRequirementItemRequest>,

    @field:NotNull
    val job: List<UpdatePartInterviewRequirementItemRequest>,

    @field:NotNull
    val other: List<UpdatePartInterviewRequirementItemRequest>,
)
