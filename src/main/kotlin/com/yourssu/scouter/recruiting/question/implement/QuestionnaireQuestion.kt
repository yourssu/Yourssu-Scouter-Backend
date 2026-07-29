package com.yourssu.scouter.recruiting.question.implement

import com.yourssu.scouter.recruiting.support.implement.exception.QuestionnaireGroupInvalidException

class QuestionnaireQuestion(
    val id: Long? = null,
    val questionnaireId: Long,
    val group: QuestionGroup,
    val sourceQuestionId: Long?,
    val content: String?,
    val sortOrder: Int,
) {

    init {
        if (group.isFixed) {
            if (sourceQuestionId == null) {
                throw QuestionnaireGroupInvalidException("고정 그룹($group) 문항은 sourceQuestionId가 필요합니다.")
            }
        } else {
            if (content.isNullOrBlank()) {
                throw QuestionnaireGroupInvalidException("자유 그룹($group) 문항은 content가 필요합니다.")
            }
        }
    }
}
