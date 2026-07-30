package com.yourssu.scouter.recruiting.question.implement

interface QuestionnaireQuestionRequirementRepository {
    fun findAllByQuestionQuestionIds(questionnaireQuestionIds: List<Long>): List<QuestionnaireQuestionRequirement>
    fun saveAll(mappings: List<QuestionnaireQuestionRequirement>): List<QuestionnaireQuestionRequirement>
    fun deleteAllByQuestionQuestionIds(questionnaireQuestionIds: List<Long>)
}
