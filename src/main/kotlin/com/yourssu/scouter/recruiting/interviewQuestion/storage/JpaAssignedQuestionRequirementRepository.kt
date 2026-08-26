package com.yourssu.scouter.recruiting.interviewQuestion.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaAssignedQuestionRequirementRepository :
    JpaRepository<AssignedQuestionRequirementEntity, AssignedQuestionRequirementId> {

    fun findAllByAssignedQuestionIdIn(assignedQuestionIds: List<Long>): List<AssignedQuestionRequirementEntity>

    fun deleteAllByAssignedQuestionIdIn(assignedQuestionIds: List<Long>)
}
