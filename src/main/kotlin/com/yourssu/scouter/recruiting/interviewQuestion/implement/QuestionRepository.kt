package com.yourssu.scouter.recruiting.interviewQuestion.implement

interface QuestionRepository {

    fun findAll(): List<Question>

    fun findAllByIdIn(ids: Collection<Long>): List<Question>

    fun update(question: Question)
}
