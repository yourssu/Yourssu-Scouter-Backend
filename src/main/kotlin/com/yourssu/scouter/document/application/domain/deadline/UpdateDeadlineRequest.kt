package com.yourssu.scouter.document.application.domain.deadline

import jakarta.validation.constraints.NotNull
import java.time.Instant

data class UpdateDeadlineRequest(

    @field:NotNull
    val deadline: Instant?,
)
