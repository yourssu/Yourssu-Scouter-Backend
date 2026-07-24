package com.yourssu.scouter.recruiting.interview.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class InterviewScriptWriter(
    private val interviewScriptRepository: InterviewScriptRepository,
) {

    fun upsert(script: InterviewScript): InterviewScript {
        return interviewScriptRepository.upsert(script)
    }
}
