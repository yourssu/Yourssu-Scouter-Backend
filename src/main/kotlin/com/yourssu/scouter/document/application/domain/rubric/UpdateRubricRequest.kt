package com.yourssu.scouter.document.application.domain.rubric

import com.yourssu.scouter.document.business.domain.rubric.UpdateRubricItemCommand
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
