package com.yourssu.scouter.recruiting.interview.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class InterviewScriptReader(
    private val interviewScriptRepository: InterviewScriptRepository,
) {

    fun readByPartId(partId: Long): InterviewScript? {
        return interviewScriptRepository.findByPartId(partId)
    }
}
