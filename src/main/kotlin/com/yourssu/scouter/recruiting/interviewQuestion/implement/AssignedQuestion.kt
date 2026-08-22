package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionInvalidException

class AssignedQuestion(
    val id: Long? = null,
    val assignedMemberId: Long,
    val applicantId: Long,
    val sourceQuestionId: Long?,
    val content: String?,
    val category: AssignedQuestionCategory,
    val sortOrder: Int,
    val isSelected: Boolean? = null,
    val requirementIds: List<Long> = emptyList(),
) {

    init {
        if (category.isCatalog) {
            if (sourceQuestionId == null) {
                throw AssignedQuestionInvalidException("카탈로그 질문($category)은 sourceQuestionId가 필요합니다.")
            }
            if (content != null) {
                throw AssignedQuestionInvalidException("$category 질문은 content를 직접 가질 수 없습니다.")
            }
            if (requirementIds.isNotEmpty()) {
                throw AssignedQuestionInvalidException("$category 질문은 요구조건을 직접 가질 수 없습니다. 카탈로그에서 관리됩니다.")
            }
        } else {
            if (sourceQuestionId != null) {
                throw AssignedQuestionInvalidException("개인 질문은 sourceQuestionId를 가질 수 없습니다.")
            }
            if (content.isNullOrBlank()) {
                throw AssignedQuestionInvalidException("개인 질문은 content가 필요합니다.")
            }
            if (requirementIds.isEmpty()) {
                throw AssignedQuestionInvalidException("개인 질문은 최소 1개 이상의 요구조건이 필요합니다.")
            }
        }
    }
}
