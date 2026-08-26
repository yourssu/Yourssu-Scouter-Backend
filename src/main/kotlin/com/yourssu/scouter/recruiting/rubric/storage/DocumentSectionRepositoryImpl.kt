package com.yourssu.scouter.recruiting.rubric.storage

import com.yourssu.scouter.masterdata.part.storage.JpaPartRepository
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSection
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionRepository
import org.springframework.stereotype.Repository

@Repository
class DocumentSectionRepositoryImpl(
    private val jpaDocumentSectionRepository: JpaDocumentSectionRepository,
    private val jpaPartRepository: JpaPartRepository,
) : DocumentSectionRepository {

    override fun findAllByPartId(partId: Long): List<DocumentSection> {
        return jpaDocumentSectionRepository.findAllByPartId(partId).map { it.toDomain() }
    }

    override fun findAllByIdIn(sectionIds: List<Long>): List<DocumentSection> {
        return jpaDocumentSectionRepository.findAllByIdIn(sectionIds).map { it.toDomain() }
    }

    override fun saveAll(sections: List<DocumentSection>): List<DocumentSection> {
        val entities = sections.map { section ->
            DocumentSectionEntity(
                id = section.id,
                part = jpaPartRepository.getReferenceById(section.partId),
                question = section.question,
                maxScore = section.maxScore,
                criterionDetail = section.criterionDetail,
            )
        }

        return jpaDocumentSectionRepository.saveAll(entities).map { it.toDomain() }
    }
}
