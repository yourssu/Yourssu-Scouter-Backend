package com.yourssu.scouter.recruiting.interviewQuestion.implement

interface QuestionRepository {

    fun findAll(): List<Question>

    fun findAllByIdIn(ids: Collection<Long>): List<Question>

    /** CULTURE/PART 질문을 학기 ID 기준으로 조회합니다. */
    fun findAllBySemesterId(semesterId: Long): List<Question>

    /** partId 와 semesterId 를 모두 만족하는 PART 질문을 조회합니다. */
    fun findAllByPartIdAndSemesterId(partId: Long, semesterId: Long): List<Question>

    fun update(question: Question)

    fun save(question: Question): Question

    fun deleteAllByIdIn(ids: Collection<Long>)
}

