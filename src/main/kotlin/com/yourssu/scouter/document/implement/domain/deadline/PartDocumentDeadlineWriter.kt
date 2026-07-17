package com.yourssu.scouter.document.implement.domain.deadline

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class PartDocumentDeadlineWriter(
    private val partDocumentDeadlineRepository: PartDocumentDeadlineRepository,
) {

    fun upsert(deadline: PartDocumentDeadline): PartDocumentDeadline {
        return partDocumentDeadlineRepository.upsert(deadline)
    }
}
