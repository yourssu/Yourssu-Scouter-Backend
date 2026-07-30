package com.yourssu.scouter.recruiting.rubric.implement

import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import java.time.Instant

class InterviewRubric(
    val id: Long? = null,
    val partId: Long,
    val semester: Semester,
    val deadline: Instant,
    val isLocked: Boolean = false,
    val items: List<InterviewEvaluationItem>,
) {

    fun validateTotalScore() {
        // 배점 합계 100점 비즈니스 검증
        val totalScore = items.sumOf { it.maxScore }
        require(totalScore == 100) {
            "면접 평가 항목의 총 배점은 100점이어야 합니다. (현재 합계: ${totalScore}점)"
        }
    }

    fun validateEditable() {
        check(!isLocked) { "이미 평가가 진행 중이거나 잠긴 루브릭은 수정할 수 없습니다." }
    }


    fun update(
        semester: Semester,
        deadline: Instant,
        items: List<InterviewEvaluationItem>
    ): InterviewRubric {
        validateEditable()

        return InterviewRubric(
            id = this.id,
            partId = this.partId,
            semester = semester,
            deadline = deadline,
            isLocked = this.isLocked,
            items = items
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterviewRubric

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
