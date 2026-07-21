package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.recruiting.rubric.implement.DocumentSection

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
