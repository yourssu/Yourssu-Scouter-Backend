package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class QuestionWriter(
    private val questionRepository: QuestionRepository,
) {

    fun save(question: Question): Question = questionRepository.save(question)

    fun update(question: Question) {
        questionRepository.update(question)
    }

    fun deleteAllByIdIn(ids: Collection<Long>) = questionRepository.deleteAllByIdIn(ids)
}
