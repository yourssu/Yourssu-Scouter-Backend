package com.yourssu.scouter.ats.application.domain.applicant

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class LastApplicantSyncTimeResponse(

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    val lastUpdatedTime: LocalDateTime?,
)
