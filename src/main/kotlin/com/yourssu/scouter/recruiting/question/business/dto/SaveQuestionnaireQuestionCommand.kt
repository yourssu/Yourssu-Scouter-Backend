package com.yourssu.scouter.recruiting.question.business.dto

import com.yourssu.scouter.recruiting.question.implement.QuestionGroup

data class SaveQuestionnaireQuestionCommand(
    val group: QuestionGroup,
    val sourceQuestionId: Long?,
    val content: String?,
)
