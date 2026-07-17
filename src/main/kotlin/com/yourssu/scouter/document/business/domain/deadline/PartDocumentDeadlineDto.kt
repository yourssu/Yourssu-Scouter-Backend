package com.yourssu.scouter.document.business.domain.deadline

import com.yourssu.scouter.document.implement.domain.deadline.PartDocumentDeadline
import java.time.Instant

data class PartDocumentDeadlineDto(
    val deadline: Instant?,
) {
    companion object {
        fun from(deadline: PartDocumentDeadline?): PartDocumentDeadlineDto = PartDocumentDeadlineDto(deadline?.deadline)
    }
}
