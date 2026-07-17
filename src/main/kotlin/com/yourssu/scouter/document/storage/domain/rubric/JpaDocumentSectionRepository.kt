package com.yourssu.scouter.document.storage.domain.rubric

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDocumentSectionRepository : JpaRepository<DocumentSectionEntity, Long> {

    fun findAllByPartId(partId: Long): List<DocumentSectionEntity>

    fun findAllByIdIn(sectionIds: List<Long>): List<DocumentSectionEntity>
}
