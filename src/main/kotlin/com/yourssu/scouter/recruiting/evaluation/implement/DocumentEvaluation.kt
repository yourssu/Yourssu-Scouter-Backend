package com.yourssu.scouter.recruiting.evaluation.implement

import java.time.Instant

class DocumentEvaluation(
    val id: Long? = null,
    val applicantId: Long,
    val evaluatorUserId: Long,
    val items: List<DocumentEvaluationItem>,
    val overallComment: String,
    val result: DocumentResult,
    val submittedAt: Instant?,
) {

    fun totalScore(): Int = items.sumOf { it.score }

    fun isSubmitted(): Boolean = submittedAt != null

    // 조회자가 본인 평가를 제출하기 전이면 overallComment/items[].memo를 마스킹한다 [A5]
    fun maskedFor(viewerHasSubmitted: Boolean): DocumentEvaluation {
        if (viewerHasSubmitted) {
            return this
        }

        return DocumentEvaluation(
            id = id,
            applicantId = applicantId,
            evaluatorUserId = evaluatorUserId,
            items = items.map { DocumentEvaluationItem(it.sectionId, it.score, "") },
            overallComment = "",
            result = result,
            submittedAt = submittedAt,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentEvaluation

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
