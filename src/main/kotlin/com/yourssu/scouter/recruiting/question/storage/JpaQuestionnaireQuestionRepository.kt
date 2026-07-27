package com.yourssu.scouter.recruiting.question.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaQuestionnaireQuestionRepository : JpaRepository<QuestionnaireQuestionEntity, Long> {

    fun findAllByQuestionnaireId(questionnaireId: Long): List<QuestionnaireQuestionEntity>

    fun deleteAllByQuestionnaireId(questionnaireId: Long)
}
