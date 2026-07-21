package com.yourssu.scouter.recruiting.deadline.application

import com.yourssu.scouter.recruiting.deadline.business.PartDocumentDeadlineDto
import java.time.Instant

data class ReadDeadlineResponse(
    val deadline: Instant?,
) {
    companion object {
        fun from(dto: PartDocumentDeadlineDto): ReadDeadlineResponse = ReadDeadlineResponse(dto.deadline)
    }
}
