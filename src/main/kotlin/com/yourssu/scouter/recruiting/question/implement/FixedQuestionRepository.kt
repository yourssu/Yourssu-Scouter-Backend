package com.yourssu.scouter.recruiting.question.implement

interface FixedQuestionRepository {

    fun findAll(): List<FixedQuestion>

    fun findAllByIdIn(ids: Collection<Long>): List<FixedQuestion>
}
