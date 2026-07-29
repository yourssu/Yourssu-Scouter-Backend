package com.yourssu.scouter.recruiting.interview.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaInterviewMemoRepository : JpaRepository<InterviewMemoEntity, Long> {

    fun findAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds: List<Long>): List<InterviewMemoEntity>

    fun deleteAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds: List<Long>)
}
