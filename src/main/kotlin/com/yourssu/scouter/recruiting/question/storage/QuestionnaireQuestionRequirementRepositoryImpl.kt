package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestionRequirement
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestionRequirementRepository
import org.springframework.stereotype.Repository

@Repository
class QuestionnaireQuestionRequirementRepositoryImpl(
    private val jpaRepository: JpaQuestionnaireQuestionRequirementRepository,
) : QuestionnaireQuestionRequirementRepository {

    override fun findAllByQuestionQuestionIds(questionnaireQuestionIds: List<Long>): List<QuestionnaireQuestionRequirement> {
        if (questionnaireQuestionIds.isEmpty()) return emptyList()
        return jpaRepository.findAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds)
            .map { it.toDomain() }
    }

    override fun saveAll(mappings: List<QuestionnaireQuestionRequirement>): List<QuestionnaireQuestionRequirement> {
        val entities = mappings.map { QuestionnaireQuestionRequirementEntity.from(it) }
        return jpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun deleteAllByQuestionQuestionIds(questionnaireQuestionIds: List<Long>) {
        if (questionnaireQuestionIds.isEmpty()) return
        jpaRepository.deleteAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds)
    }
}
