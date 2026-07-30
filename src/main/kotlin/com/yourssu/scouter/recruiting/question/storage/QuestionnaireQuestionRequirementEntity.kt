package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestionRequirement
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "questionnaire_question_requirement")
class QuestionnaireQuestionRequirementEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "questionnaire_question_id", nullable = false)
    val questionnaireQuestionId: Long,

    @Column(name = "part_interview_requirement_id", nullable = false)
    val partInterviewRequirementId: Long,
) {
    fun toDomain(): QuestionnaireQuestionRequirement = QuestionnaireQuestionRequirement(
        id = id,
        questionnaireQuestionId = questionnaireQuestionId,
        partInterviewRequirementId = partInterviewRequirementId,
    )

    companion object {
        fun from(domain: QuestionnaireQuestionRequirement): QuestionnaireQuestionRequirementEntity =
            QuestionnaireQuestionRequirementEntity(
                id = domain.id,
                questionnaireQuestionId = domain.questionnaireQuestionId,
                partInterviewRequirementId = domain.partInterviewRequirementId,
            )
    }
}
