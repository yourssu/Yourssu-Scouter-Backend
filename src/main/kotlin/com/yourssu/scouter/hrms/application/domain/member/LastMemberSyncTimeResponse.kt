package com.yourssu.scouter.hrms.application.domain.member

import java.time.Instant

data class LastMemberSyncTimeResponse(

    val lastUpdatedTime: Instant?,
)
