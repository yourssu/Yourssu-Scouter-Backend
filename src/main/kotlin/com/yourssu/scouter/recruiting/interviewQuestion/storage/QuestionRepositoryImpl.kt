package com.yourssu.scouter.recruiting.interviewQuestion.storage

import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionRepository
import org.springframework.stereotype.Repository

@Repository
class QuestionRepositoryImpl(
    private val jpaQuestionRepository: JpaQuestionRepository,
    private val jpaQuestionRequirementRepository: JpaQuestionRequirementRepository,
) : QuestionRepository {

    override fun findAll(): List<Question> {
        val entities = jpaQuestionRepository.findAll()
        val requirementIdsByQuestionId = readRequirementIdsByQuestionId(entities.mapNotNull { it.id })

        return entities.map { it.toDomain(requirementIdsByQuestionId[it.id].orEmpty()) }
    }

    override fun findAllByIdIn(ids: Collection<Long>): List<Question> {
        val entities = jpaQuestionRepository.findAllByIdIn(ids)
        val requirementIdsByQuestionId = readRequirementIdsByQuestionId(entities.mapNotNull { it.id })

        return entities.map { it.toDomain(requirementIdsByQuestionId[it.id].orEmpty()) }
    }

    override fun update(question: Question) {
        val questionId = question.id!!

        jpaQuestionRepository.save(
            QuestionEntity(
                id = questionId,
                partId = question.partId,
                category = question.category,
                content = question.content,
                sortOrder = question.sortOrder,
            ),
        )

        jpaQuestionRequirementRepository.deleteAllByQuestionIdIn(listOf(questionId))
        jpaQuestionRequirementRepository.saveAll(
            question.requirementIds.map { requirementId -> QuestionRequirementEntity(questionId, requirementId) },
        )
    }

    private fun readRequirementIdsByQuestionId(questionIds: List<Long>): Map<Long, List<Long>> {
        if (questionIds.isEmpty()) {
            return emptyMap()
        }

        return jpaQuestionRequirementRepository.findAllByQuestionIdIn(questionIds)
            .groupBy { it.questionId }
            .mapValues { (_, mappings) -> mappings.map { it.partInterviewRequirementId } }
    }
}
