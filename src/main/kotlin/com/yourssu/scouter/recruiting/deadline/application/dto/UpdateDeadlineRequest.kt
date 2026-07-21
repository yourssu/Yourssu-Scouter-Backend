package com.yourssu.scouter.recruiting.deadline.application.dto

import jakarta.validation.constraints.NotNull
import java.time.Instant

data class UpdateDeadlineRequest(

    @field:NotNull
    val deadline: Instant?,
)
