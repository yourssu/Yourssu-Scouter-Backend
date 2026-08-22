package com.yourssu.scouter.recruiting.interviewQuestion.storage

import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionRepository
import org.springframework.stereotype.Repository

@Repository
class PartCultureSelectionRepositoryImpl(
    private val jpaPartCultureQuestionSelectionRepository: JpaPartCultureQuestionSelectionRepository,
) : PartCultureSelectionRepository {

    override fun findSelectedQuestionIds(partId: Long, semesterId: Long): Set<Long> {
        return jpaPartCultureQuestionSelectionRepository
            .findAllByPartIdAndSemesterId(partId, semesterId)
            .map { it.questionId }
            .toSet()
    }

    override fun replaceSelection(partId: Long, semesterId: Long, questionIds: List<Long>) {
        jpaPartCultureQuestionSelectionRepository.deleteAllByPartIdAndSemesterId(partId, semesterId)
        jpaPartCultureQuestionSelectionRepository.saveAll(
            questionIds.map { questionId ->
                PartCultureQuestionSelectionEntity(partId = partId, semesterId = semesterId, questionId = questionId)
            },
        )
    }

    override fun deleteAllByQuestionIdIn(questionIds: List<Long>) {
        if (questionIds.isEmpty()) return
        jpaPartCultureQuestionSelectionRepository.deleteAllByQuestionIdIn(questionIds)
    }
}
