package com.yourssu.scouter.recruiting.interviewQuestion.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaQuestionRequirementRepository : JpaRepository<QuestionRequirementEntity, QuestionRequirementId> {

    fun findAllByQuestionIdIn(questionIds: List<Long>): List<QuestionRequirementEntity>

    fun findAllByPartInterviewRequirementIdIn(requirementIds: List<Long>): List<QuestionRequirementEntity>

    fun deleteAllByQuestionIdIn(questionIds: List<Long>)
}
