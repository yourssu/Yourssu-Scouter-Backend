package com.yourssu.scouter.document.business.domain.rubric

data class UpdateRubricItemCommand(
    val sectionId: Long,
    val maxScore: Int,
    val criterionDetail: String,
)
