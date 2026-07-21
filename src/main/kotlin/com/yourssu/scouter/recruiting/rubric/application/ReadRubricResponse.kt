package com.yourssu.scouter.recruiting.rubric.application

import com.yourssu.scouter.recruiting.rubric.business.DocumentSectionDto

data class ReadRubricResponse(
    val sectionId: Long,
    val question: String,
    val maxScore: Int,
    val criterionDetail: String,
) {
    companion object {
        fun from(dto: DocumentSectionDto): ReadRubricResponse = ReadRubricResponse(
            sectionId = dto.sectionId,
            question = dto.question,
            maxScore = dto.maxScore,
            criterionDetail = dto.criterionDetail,
        )
    }
}
