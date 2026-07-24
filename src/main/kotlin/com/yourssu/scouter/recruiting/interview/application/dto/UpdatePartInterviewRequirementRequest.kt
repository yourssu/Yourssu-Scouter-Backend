package com.yourssu.scouter.recruiting.interview.application.dto

import jakarta.validation.constraints.NotBlank

data class UpdatePartInterviewRequirementRequest(

    @field:NotBlank
    val culture: String?,

    @field:NotBlank
    val team: String?,

    @field:NotBlank
    val job: String?,
)
