package com.yourssu.scouter.recruiting.question.implement

interface QuestionnaireQuestionRepository {

    fun findAllByQuestionnaireId(questionnaireId: Long): List<QuestionnaireQuestion>

    fun replaceAll(questionnaireId: Long, questions: List<QuestionnaireQuestion>): List<QuestionnaireQuestion>
}
