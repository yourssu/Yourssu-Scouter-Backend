package com.yourssu.scouter.recruiting.interviewQuestion.storage

import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestion
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionRepository
import org.springframework.stereotype.Repository

@Repository
class AssignedQuestionRepositoryImpl(
    private val jpaAssignedQuestionRepository: JpaAssignedQuestionRepository,
    private val jpaAssignedQuestionRequirementRepository: JpaAssignedQuestionRequirementRepository,
) : AssignedQuestionRepository {

    override fun findAllByApplicantId(applicantId: Long): List<AssignedQuestion> {
        val entities = jpaAssignedQuestionRepository.findAllByApplicantId(applicantId)
        val requirementIdsByQuestionId = readRequirementIdsByQuestionId(entities.mapNotNull { it.id })

        return entities.map { entity ->
            entity.toDomain(requirementIdsByQuestionId[entity.id].orEmpty())
        }
    }

    override fun replaceAll(applicantId: Long, questions: List<AssignedQuestion>): List<AssignedQuestion> {
        val existingQuestionIds = jpaAssignedQuestionRepository
            .findAllByApplicantId(applicantId)
            .mapNotNull { it.id }
        jpaAssignedQuestionRequirementRepository.deleteAllByAssignedQuestionIdIn(existingQuestionIds)
        jpaAssignedQuestionRepository.deleteAllByApplicantId(applicantId)

        val entities = questions.map { question ->
            AssignedQuestionEntity(
                assignedInterviewerUserId = question.assignedInterviewerUserId,
                applicantId = applicantId,
                category = question.category,
                sourceQuestionId = question.sourceQuestionId,
                content = question.content,
                sortOrder = question.sortOrder,
                isSelected = question.isSelected,
            )
        }

        val savedEntities = jpaAssignedQuestionRepository.saveAll(entities)
        val mappings = savedEntities.zip(questions)
            .flatMap { (entity, question) ->
                question.requirementIds.map { requirementId ->
                    AssignedQuestionRequirementEntity(entity.id!!, requirementId)
                }
            }
        jpaAssignedQuestionRequirementRepository.saveAll(mappings)

        return savedEntities.zip(questions).map { (entity, question) ->
            entity.toDomain(question.requirementIds)
        }
    }

    private fun readRequirementIdsByQuestionId(questionIds: List<Long>): Map<Long, List<Long>> {
        if (questionIds.isEmpty()) {
            return emptyMap()
        }

        return jpaAssignedQuestionRequirementRepository.findAllByAssignedQuestionIdIn(questionIds)
            .groupBy { it.assignedQuestionId }
            .mapValues { (_, mappings) -> mappings.map { it.partInterviewRequirementId } }
    }
}
