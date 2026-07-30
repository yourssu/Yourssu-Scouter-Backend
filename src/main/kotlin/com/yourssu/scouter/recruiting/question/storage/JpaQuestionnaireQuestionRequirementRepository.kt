package com.yourssu.scouter.recruiting.question.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaQuestionnaireQuestionRequirementRepository : JpaRepository<QuestionnaireQuestionRequirementEntity, Long> {
    fun findAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds: List<Long>): List<QuestionnaireQuestionRequirementEntity>
    fun deleteAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds: List<Long>)
}
