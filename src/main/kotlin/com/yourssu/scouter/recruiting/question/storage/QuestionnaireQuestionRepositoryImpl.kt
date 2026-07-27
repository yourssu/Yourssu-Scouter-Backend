package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestion
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestionRepository
import org.springframework.stereotype.Repository

@Repository
class QuestionnaireQuestionRepositoryImpl(
    private val jpaQuestionnaireQuestionRepository: JpaQuestionnaireQuestionRepository,
) : QuestionnaireQuestionRepository {

    override fun findAllByQuestionnaireId(questionnaireId: Long): List<QuestionnaireQuestion> {
        return jpaQuestionnaireQuestionRepository.findAllByQuestionnaireId(questionnaireId).map { it.toDomain() }
    }

    override fun replaceAll(questionnaireId: Long, questions: List<QuestionnaireQuestion>): List<QuestionnaireQuestion> {
        jpaQuestionnaireQuestionRepository.deleteAllByQuestionnaireId(questionnaireId)

        val entities = questions.map { question ->
            QuestionnaireQuestionEntity(
                questionnaireId = questionnaireId,
                group = question.group,
                sourceQuestionId = question.sourceQuestionId,
                content = question.content,
                sortOrder = question.sortOrder,
            )
        }

        return jpaQuestionnaireQuestionRepository.saveAll(entities).map { it.toDomain() }
    }
}
