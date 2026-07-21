package com.yourssu.scouter.recruiting.rubric.implement

interface DocumentSectionRepository {

    fun findAllByPartId(partId: Long): List<DocumentSection>

    fun findAllByIdIn(sectionIds: List<Long>): List<DocumentSection>

    fun saveAll(sections: List<DocumentSection>): List<DocumentSection>
}
