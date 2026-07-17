package com.yourssu.scouter.document.implement.domain.deadline

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class PartDocumentDeadlineReader(
    private val partDocumentDeadlineRepository: PartDocumentDeadlineRepository,
) {

    fun readByPartId(partId: Long): PartDocumentDeadline? {
        return partDocumentDeadlineRepository.findByPartId(partId)
    }
}
