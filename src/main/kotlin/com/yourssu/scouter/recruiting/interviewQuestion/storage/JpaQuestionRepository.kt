package com.yourssu.scouter.recruiting.interviewQuestion.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaQuestionRepository : JpaRepository<QuestionEntity, Long> {

    fun findAllByIdIn(ids: Collection<Long>): List<QuestionEntity>

    fun findAllBySemesterId(semesterId: Long): List<QuestionEntity>

    fun findAllByPartIdAndSemesterId(partId: Long, semesterId: Long): List<QuestionEntity>
}

