package com.yourssu.scouter.recruiting.rubric.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import java.time.Instant

data class UpdateInterviewRubricCommand(
    val partId: Long,
    val semester: String,
    val deadline: Instant,
    val interviewerCount: Int,
    val items: List<ItemCommand>
) {
    data class ItemCommand(
        val id: Long? = null,
        val keyword: String,
        val rubricType: RubricGroupType,
        val maxScore: Int
    )

    fun toDomain(existingId: Long?, isLocked: Boolean = false): InterviewRubric {
        return InterviewRubric(
            id = existingId,
            partId = partId,
            semester = semester,
            deadline = deadline,
            isLocked = isLocked,
            interviewerCount = interviewerCount,
            items = items.map {
                InterviewEvaluationItem(
                    id = it.id,
                    keyword = it.keyword,
                    rubricType = it.rubricType,
                    maxScore = it.maxScore
                )
            }
        )
    }
}
