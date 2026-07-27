package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.QuestionGroup
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestion
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "questionnaire_question")
class QuestionnaireQuestionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "questionnaire_id", nullable = false)
    val questionnaireId: Long,

    @Column(name = "question_group", nullable = false)
    @Enumerated(EnumType.STRING)
    val group: QuestionGroup,

    @Column(name = "source_question_id")
    val sourceQuestionId: Long?,

    @Column(columnDefinition = "TEXT")
    val content: String?,

    @Column(nullable = false)
    val sortOrder: Int,
) {

    fun toDomain(): QuestionnaireQuestion = QuestionnaireQuestion(
        id = id,
        questionnaireId = questionnaireId,
        group = group,
        sourceQuestionId = sourceQuestionId,
        content = content,
        sortOrder = sortOrder,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuestionnaireQuestionEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
