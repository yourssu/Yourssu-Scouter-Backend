package com.yourssu.scouter.recruiting.rubric.application.dto

import com.yourssu.scouter.recruiting.rubric.business.dto.UpdateRubricItemCommand
import jakarta.validation.constraints.NotNull

data class UpdateRubricRequest(

    @field:NotNull
    val sectionId: Long?,

    @field:NotNull
    val maxScore: Int?,

    @field:NotNull
    val criterionDetail: String?,
) {
    fun toCommand(): UpdateRubricItemCommand = UpdateRubricItemCommand(
        sectionId = sectionId!!,
        maxScore = maxScore!!,
        criterionDetail = criterionDetail!!,
    )
}
