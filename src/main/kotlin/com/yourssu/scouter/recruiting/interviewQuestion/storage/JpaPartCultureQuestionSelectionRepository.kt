package com.yourssu.scouter.recruiting.interviewQuestion.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaPartCultureQuestionSelectionRepository :
    JpaRepository<PartCultureQuestionSelectionEntity, PartCultureQuestionSelectionId> {

    fun findAllByPartIdAndSemesterId(partId: Long, semesterId: Long): List<PartCultureQuestionSelectionEntity>

    fun deleteAllByPartIdAndSemesterId(partId: Long, semesterId: Long)

    fun deleteAllByQuestionIdIn(questionIds: List<Long>)
}
