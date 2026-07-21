package com.yourssu.scouter.recruiting.rubric.business.dto

data class UpdateRubricItemCommand(
    val sectionId: Long,
    val maxScore: Int,
    val criterionDetail: String,
)
