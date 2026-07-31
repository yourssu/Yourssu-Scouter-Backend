package com.yourssu.scouter.recruiting.interviewQuestion.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaAssignedQuestionRepository : JpaRepository<AssignedQuestionEntity, Long> {

    fun findAllByApplicantId(applicantId: Long): List<AssignedQuestionEntity>

    fun deleteAllByApplicantId(applicantId: Long)
}
