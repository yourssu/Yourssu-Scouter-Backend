package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class PartCultureSelectionReader(
    private val partCultureSelectionRepository: PartCultureSelectionRepository,
) {

    fun readSelectedQuestionIds(partId: Long, semesterId: Long): Set<Long> {
        return partCultureSelectionRepository.findSelectedQuestionIds(partId, semesterId)
    }
}
