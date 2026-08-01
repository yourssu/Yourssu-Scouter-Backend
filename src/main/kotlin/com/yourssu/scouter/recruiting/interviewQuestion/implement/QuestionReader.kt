package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class QuestionReader(
    private val questionRepository: QuestionRepository,
) {

    fun readAll(): List<Question> {
        return questionRepository.findAll().sortedWith(compareBy({ it.category }, { it.partId ?: 0L }, { it.sortOrder }))
    }

    fun readAllByIdIn(ids: Collection<Long>): List<Question> {
        return questionRepository.findAllByIdIn(ids)
    }
}
