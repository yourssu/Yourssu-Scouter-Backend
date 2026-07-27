package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.Questionnaire
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class QuestionnaireRepositoryImpl(
    private val jpaQuestionnaireRepository: JpaQuestionnaireRepository,
) : QuestionnaireRepository {

    override fun findByApplicantId(applicantId: Long): Questionnaire? {
        return jpaQuestionnaireRepository.findByIdOrNull(applicantId)?.toDomain()
    }

    override fun upsert(questionnaire: Questionnaire): Questionnaire {
        val entity = QuestionnaireEntity(
            applicantId = questionnaire.applicantId,
            assignedInterviewerUserId = questionnaire.assignedInterviewerUserId,
        )

        return jpaQuestionnaireRepository.save(entity).toDomain()
    }
}
