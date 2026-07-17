package com.yourssu.scouter.document.business.domain.rubric

import com.yourssu.scouter.document.implement.domain.rubric.DocumentSection

data class DocumentSectionDto(
    val sectionId: Long,
    val question: String,
    val maxScore: Int,
    val criterionDetail: String,
) {
    companion object {
        fun from(documentSection: DocumentSection): DocumentSectionDto = DocumentSectionDto(
            sectionId = documentSection.id!!,
            question = documentSection.question,
            maxScore = documentSection.maxScore,
            criterionDetail = documentSection.criterionDetail,
        )
    }
}
