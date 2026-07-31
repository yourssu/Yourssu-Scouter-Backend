package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class AssignedQuestionReader(
    private val assignedQuestionRepository: AssignedQuestionRepository,
) {

    fun readAllByApplicantId(applicantId: Long): List<AssignedQuestion> {
        return assignedQuestionRepository.findAllByApplicantId(applicantId)
    }
}
