package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.Questionnaire
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "questionnaire")
class QuestionnaireEntity(

    @Id
    @Column(name = "applicant_id")
    val applicantId: Long,

    @Column(name = "assigned_interviewer_user_id", nullable = false)
    val assignedInterviewerUserId: Long,
) {

    fun toDomain(): Questionnaire = Questionnaire(
        applicantId = applicantId,
        assignedInterviewerUserId = assignedInterviewerUserId,
    )
}
