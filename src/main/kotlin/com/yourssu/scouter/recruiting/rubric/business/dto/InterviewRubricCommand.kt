package com.yourssu.scouter.recruiting.rubric.business.dto

import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import java.time.Instant

data class UpdateInterviewRubricCommand(
    val partId: Long,
    val semester: String,
    val deadline: Instant,
    val items: List<ItemCommand>
) {
    data class ItemCommand(
        val id: Long,
        val maxScore: Int
    )
}
