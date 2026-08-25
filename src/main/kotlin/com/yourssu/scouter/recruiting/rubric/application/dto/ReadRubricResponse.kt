package com.yourssu.scouter.recruiting.rubric.application.dto

import com.yourssu.scouter.recruiting.rubric.business.dto.DocumentRubricsResult
import com.yourssu.scouter.recruiting.rubric.business.dto.DocumentSectionDto

data class ReadRubricResponse(
    val isLocked: Boolean,
    val rubrics: List<RubricResponse>,
) {
    data class RubricResponse(
        val sectionId: Long,
        val question: String,
        val maxScore: Int,
        val criterionDetail: String,
    ) {
        companion object {
            fun from(dto: DocumentSectionDto): RubricResponse = RubricResponse(
                sectionId = dto.sectionId,
                question = dto.question,
                maxScore = dto.maxScore,
                criterionDetail = dto.criterionDetail,
            )
        }
    }

    companion object {
        fun from(result: DocumentRubricsResult): ReadRubricResponse = ReadRubricResponse(
            isLocked = result.isLocked,
            rubrics = result.sections.map(RubricResponse::from),
        )
    }
}
