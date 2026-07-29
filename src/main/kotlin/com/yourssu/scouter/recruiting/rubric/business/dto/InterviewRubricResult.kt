package com.yourssu.scouter.recruiting.rubric.business.dto


import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import java.time.Instant

data class InterviewRubricResult(
    val id: Long,
    val partId: Long,
    val semester: String,
    val deadline: Instant,
    val isLocked: Boolean,
    val items: List<ItemResult>
) {
    data class ItemResult(
        val id: Long,
        val keyword: String,
        val rubricType: RubricGroupType,
        val maxScore: Int
    )

    companion object {
        fun from(domain: InterviewRubric): InterviewRubricResult {
            return InterviewRubricResult(
                id = domain.id ?: 0L,
                partId = domain.partId,
                semester = "${domain.semester.year.value}-${domain.semester.term.intValue}",
                deadline = domain.deadline,
                isLocked = domain.isLocked,
                items = domain.items.map {
                    ItemResult(
                        id = it.id ?: 0L,
                        keyword = it.keyword,
                        rubricType = it.rubricType,
                        maxScore = it.maxScore
                    )
                }
            )
        }
    }
}
