package com.yourssu.scouter.recruiting.interviewQuestion.storage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "assigned_question_requirement")
@IdClass(AssignedQuestionRequirementId::class)
class AssignedQuestionRequirementEntity(

    @Id
    @Column(name = "assigned_question_id", nullable = false)
    val assignedQuestionId: Long,

    @Id
    @Column(name = "part_interview_requirement_id", nullable = false)
    val partInterviewRequirementId: Long,
)

data class AssignedQuestionRequirementId(
    val assignedQuestionId: Long = 0,
    val partInterviewRequirementId: Long = 0,
) : Serializable
