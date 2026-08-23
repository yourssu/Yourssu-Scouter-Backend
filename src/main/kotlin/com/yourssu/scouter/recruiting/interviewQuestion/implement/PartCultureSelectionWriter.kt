package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class PartCultureSelectionWriter(
    private val partCultureSelectionRepository: PartCultureSelectionRepository,
) {

    fun replaceSelection(partId: Long, semesterId: Long, questionIds: List<Long>) {
        partCultureSelectionRepository.replaceSelection(partId, semesterId, questionIds)
    }

    fun deleteAllByQuestionIdIn(questionIds: List<Long>) {
        partCultureSelectionRepository.deleteAllByQuestionIdIn(questionIds)
    }
}
