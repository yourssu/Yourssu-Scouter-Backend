package com.yourssu.scouter.recruiting.rubric.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DocumentSectionWriter(
    private val documentSectionRepository: DocumentSectionRepository,
) {

    fun writeAll(sections: List<DocumentSection>): List<DocumentSection> {
        return documentSectionRepository.saveAll(sections)
    }
}
