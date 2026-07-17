package com.yourssu.scouter.document.implement.domain.rubric

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class DocumentSectionReader(
    private val documentSectionRepository: DocumentSectionRepository,
) {

    fun readAllByPartId(partId: Long): List<DocumentSection> {
        return documentSectionRepository.findAllByPartId(partId)
    }

    fun readAllByIdIn(sectionIds: List<Long>): List<DocumentSection> {
        return documentSectionRepository.findAllByIdIn(sectionIds)
    }
}
