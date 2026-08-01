package com.yourssu.scouter.recruiting.interview.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class InterviewMemoReader(
    private val interviewMemoRepository: InterviewMemoRepository,
) {

    fun readAllByAssignedQuestionIdIn(assignedQuestionIds: List<Long>): List<InterviewMemo> {
        return interviewMemoRepository.findAllByAssignedQuestionIdIn(assignedQuestionIds)
    }
}
