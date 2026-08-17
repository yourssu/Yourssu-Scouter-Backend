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

    override fun findAllBySemesterId(semesterId: Long): List<Question> {
        val entities = jpaQuestionRepository.findAllBySemesterId(semesterId)
        val requirementIdsByQuestionId = readRequirementIdsByQuestionId(entities.mapNotNull { it.id })

        return entities.map { it.toDomain(requirementIdsByQuestionId[it.id].orEmpty()) }
    }

    override fun findAllByPartIdAndSemesterId(partId: Long, semesterId: Long): List<Question> {
        val entities = jpaQuestionRepository.findAllByPartIdAndSemesterId(partId, semesterId)
        val requirementIdsByQuestionId = readRequirementIdsByQuestionId(entities.mapNotNull { it.id })

        return entities.map { it.toDomain(requirementIdsByQuestionId[it.id].orEmpty()) }
    }

    override fun update(question: Question) {
        val questionId = question.id!!

        jpaQuestionRepository.save(
            QuestionEntity(
                id = questionId,
                partId = question.partId,
                semesterId = question.semesterId,
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

    override fun save(question: Question): Question {
        val saved = jpaQuestionRepository.save(
            QuestionEntity(
                partId = question.partId,
                semesterId = question.semesterId,
                category = question.category,
                content = question.content,
                sortOrder = question.sortOrder,
            ),
        )
        jpaQuestionRequirementRepository.saveAll(
            question.requirementIds.map { QuestionRequirementEntity(saved.id!!, it) },
        )
        return saved.toDomain(question.requirementIds)
    }

    override fun deleteAllByIdIn(ids: Collection<Long>) {
        if (ids.isNotEmpty()) {
            jpaQuestionRequirementRepository.deleteAllByQuestionIdIn(ids.toList())
            jpaQuestionRepository.deleteAllById(ids)
        }
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

