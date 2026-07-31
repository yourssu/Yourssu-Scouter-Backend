package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class QuestionWriter(
    private val questionRepository: QuestionRepository,
) {

    fun update(question: Question) {
        questionRepository.update(question)
    }
}
