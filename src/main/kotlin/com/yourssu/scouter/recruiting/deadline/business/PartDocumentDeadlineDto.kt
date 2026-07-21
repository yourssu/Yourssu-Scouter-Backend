package com.yourssu.scouter.recruiting.deadline.business

import com.yourssu.scouter.recruiting.deadline.implement.PartDocumentDeadline
import java.time.Instant

data class PartDocumentDeadlineDto(
    val deadline: Instant?,
) {
    companion object {
        fun from(deadline: PartDocumentDeadline?): PartDocumentDeadlineDto = PartDocumentDeadlineDto(deadline?.deadline)
    }
}
