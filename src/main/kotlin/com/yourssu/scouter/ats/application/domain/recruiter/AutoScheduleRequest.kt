package com.yourssu.scouter.ats.application.domain.recruiter

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class AutoScheduleRequest(
    @field:NotNull
    @field:Schema(examples = ["MAX", "MIN"])
    val strategy: String,

    @field:NotNull
    @field:Schema(example = "5")
    val size: Int,

    @field:NotNull
    @field:Schema(example = "60")
    val duration: Long
)
