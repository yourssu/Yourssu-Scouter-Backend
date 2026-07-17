package com.yourssu.scouter.document.application.domain.deadline

import com.yourssu.scouter.document.business.domain.deadline.PartDocumentDeadlineDto
import java.time.Instant

data class ReadDeadlineResponse(
    val deadline: Instant?,
) {
    companion object {
        fun from(dto: PartDocumentDeadlineDto): ReadDeadlineResponse = ReadDeadlineResponse(dto.deadline)
    }
}
