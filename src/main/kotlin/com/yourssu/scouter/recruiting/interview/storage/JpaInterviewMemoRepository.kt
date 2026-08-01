package com.yourssu.scouter.recruiting.interview.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaInterviewMemoRepository : JpaRepository<InterviewMemoEntity, Long> {

    fun findAllByAssignedQuestionIdIn(assignedQuestionIds: List<Long>): List<InterviewMemoEntity>

    fun deleteAllByAssignedQuestionIdIn(assignedQuestionIds: List<Long>)
}
