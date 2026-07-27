package com.yourssu.scouter.recruiting.question.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class FixedQuestionReader(
    private val fixedQuestionRepository: FixedQuestionRepository,
) {

    fun readAll(): List<FixedQuestion> {
        return fixedQuestionRepository.findAll().sortedWith(compareBy({ it.category }, { it.sortOrder }))
    }

    fun readAllByIdIn(ids: Collection<Long>): List<FixedQuestion> {
        return fixedQuestionRepository.findAllByIdIn(ids)
    }
}
