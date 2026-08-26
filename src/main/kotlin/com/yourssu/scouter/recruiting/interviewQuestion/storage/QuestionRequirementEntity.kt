package com.yourssu.scouter.recruiting.interviewQuestion.storage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "question_requirement")
@IdClass(QuestionRequirementId::class)
class QuestionRequirementEntity(

    @Id
    @Column(name = "question_id", nullable = false)
    val questionId: Long,

    @Id
    @Column(name = "part_interview_requirement_id", nullable = false)
    val partInterviewRequirementId: Long,
)

data class QuestionRequirementId(
    val questionId: Long = 0,
    val partInterviewRequirementId: Long = 0,
) : Serializable
