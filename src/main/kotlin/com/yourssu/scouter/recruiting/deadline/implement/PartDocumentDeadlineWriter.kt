package com.yourssu.scouter.recruiting.deadline.implement

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
