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
        // delete-then-save 방식은 겹치는 questionId에 대해 같은 트랜잭션 안에서 remove 대기 중인
        // 엔티티에 save()가 merge()로 들어가 충돌한다. (PartCultureQuestionSelectionEntity는 @IdClass +
        // 할당형 PK라 Spring Data가 항상 merge를 탄다.) 겹치는 항목은 건드리지 않는 delta 방식으로 회피한다.
        val current = findSelectedQuestionIds(partId, semesterId)
        val target = questionIds.toSet()

        val removed = current - target
        if (removed.isNotEmpty()) {
            jpaPartCultureQuestionSelectionRepository.deleteAllByPartIdAndSemesterIdAndQuestionIdIn(
                partId,
                semesterId,
                removed.toList(),
            )
        }

        val added = target - current
        added.forEach { questionId ->
            jpaPartCultureQuestionSelectionRepository.save(
                PartCultureQuestionSelectionEntity(partId = partId, semesterId = semesterId, questionId = questionId),
            )
        }
    }

    override fun deleteAllByQuestionIdIn(questionIds: List<Long>) {
        if (questionIds.isEmpty()) return
        jpaPartCultureQuestionSelectionRepository.deleteAllByQuestionIdIn(questionIds)
    }
}
