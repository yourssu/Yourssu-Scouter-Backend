package com.yourssu.scouter.recruiting.interview.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class InterviewMemoWriter(
    private val interviewMemoRepository: InterviewMemoRepository,
) {

    fun replaceAll(questionnaireQuestionIds: List<Long>, memos: List<InterviewMemo>): List<InterviewMemo> {
        return interviewMemoRepository.replaceAll(questionnaireQuestionIds, memos)
    }
}
