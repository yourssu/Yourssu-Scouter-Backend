package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class LastMemberSyncTimeResponse(

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    val lastUpdatedTime: LocalDateTime?,
)
