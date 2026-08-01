package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AssignedQuestionWriter(
    private val assignedQuestionRepository: AssignedQuestionRepository,
) {

    fun replaceAll(applicantId: Long, questions: List<AssignedQuestion>): List<AssignedQuestion> {
        return assignedQuestionRepository.replaceAll(applicantId, questions)
    }
}
